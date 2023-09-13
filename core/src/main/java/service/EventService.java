package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Event;
import models.User;
import repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EventService {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private static final String PATH_EVENTS = "/student/getUpcomingEventsForRegistration";
    private static final String EVENT_ID = "id";
    private static final String START = "start";
    private static final String END = "end";
    private static final String EVENT_TYPE = "eventType";
    private static final String MAX_STUDENTS = "maxStudentCount";
    private static final String LOCATION = "location";
    private static final String DESCRIPTION = "description";
    private static final String CURRENT_STUDENTS = "currentStudentsCount";
    private static final String PATH_DESCRIPTION = "/activity/description";
    private static final String PATH_AUTHOR_ID = "/activity/organizers/id";

    private final EventRepository eventRepository;
    private final UserService userService;

    public void updateEvents(JsonNode eventsJson) {
        if (eventsJson.isEmpty()) {
            LOG.error("Empty events");
            return;
        }

        JsonNode eventsList = eventsJson.at(PATH_EVENTS);
        for (JsonNode eventJson : eventsList) {
            long id = eventJson.get(EVENT_ID).asLong();
            int registered = eventJson.get(CURRENT_STUDENTS).asInt();
            Optional<Event> eventOpt = eventRepository.findById(id);
            Event event = eventOpt.orElseGet(() -> create(id, eventJson));
            event.setRegistered(registered);
            eventRepository.save(event);
        }
    }

    private Event create(long id, JsonNode eventJson) {
        Event event = new Event();
        event.setId(id);
        event.setStartTime(LocalDateTime.parse(eventJson.get(START).asText()));
        event.setEndTime(LocalDateTime.parse(eventJson.get(END).asText()));
        event.setType(eventJson.get(EVENT_TYPE).asText());
        event.setTitle(eventJson.get(DESCRIPTION).asText());
        event.setDescription(eventJson.at(PATH_DESCRIPTION).asText());
        event.setMaxParticipants(eventJson.get(MAX_STUDENTS).asInt());
        event.setLocation(eventJson.get(LOCATION).asText());

        String userId = eventJson.at(PATH_AUTHOR_ID).asText();
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            event.setAuthor(userOpt.get());
        } else {
            LOG.error("No such user");
        }

        return event;
    }
}
