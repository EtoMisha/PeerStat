package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Event;
import edu.platform.models.User;
import edu.platform.parser.Parser;
import edu.platform.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    public void updateEvents(JsonNode eventsJson) {
        JsonNode eventsList = eventsJson.at(GraphQLConstants.PATH_EVENTS);
        for (JsonNode eventJson : eventsList) {
            long id = eventJson.get(GraphQLConstants.EVENT_ID).asLong();
            int registered = eventJson.get(GraphQLConstants.CURRENT_STUDENTS).asInt();
            Optional<Event> eventOpt = eventRepository.findById(id);
            Event event = eventOpt.orElseGet(() -> create(id, eventJson));
            event.setRegistered(registered);
            eventRepository.save(event);
        }
    }

    private Event create(long id, JsonNode eventJson) {
        Event event = new Event();
        event.setId(id);
        event.setStartTime(LocalDateTime.parse(eventJson.get(GraphQLConstants.START).asText()));
        event.setEndTime(LocalDateTime.parse(eventJson.get(GraphQLConstants.END).asText()));
        event.setType(eventJson.get(GraphQLConstants.EVENT_TYPE).asText());
        event.setTitle(eventJson.get(GraphQLConstants.DESCRIPTION).asText());
        event.setDescription(eventJson.at(GraphQLConstants.PATH_DESCRIPTION).asText());
        event.setMaxParticipants(eventJson.get(GraphQLConstants.MAX_STUDENTS).asInt());
        event.setLocation(eventJson.get(GraphQLConstants.LOCATION).asText());

        String userId = eventJson.at(GraphQLConstants.PATH_AUTHOR_ID).asText();
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            event.setAuthor(userOpt.get());
        } else {
            LOG.error("No such user");
        }

        return event;
    }

}
