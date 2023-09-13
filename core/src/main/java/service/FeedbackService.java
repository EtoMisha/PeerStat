package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Feedback;
import models.User;
import repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackService.class);

    private static final String FEEDBACK_CATEGORY = "categoryCode";
    private static final String FEEDBACK_VALUE = "value";
    private static final String PUNCTUAL = "punctual";
    private static final String INTERESTED = "interested";
    private static final String RIGOROUS = "rigorous";
    private static final String COURTEOUS = "courteous";

    private final FeedbackRepository feedbackRepository;

    public void createOrUpdate(User user, JsonNode feedbackJsonArr) {
        if (feedbackJsonArr.isEmpty()) {
            LOG.error("Empty feedbacks");
            return;
        }

        Map<String, Double> feedbackValues = new HashMap<>();
        Optional<Feedback> userFeedbackOpt = feedbackRepository.findByUser(user);
        for (JsonNode feedbackJson : feedbackJsonArr) {
            String category = feedbackJson.get(FEEDBACK_CATEGORY).asText();
            double value = feedbackJson.get(FEEDBACK_VALUE).asDouble();
            feedbackValues.put(category, value);
        }

        Feedback feedback = userFeedbackOpt.orElseGet(() -> create(user));
        feedback.setPunctual(feedbackValues.get(PUNCTUAL));
        feedback.setInterested(feedbackValues.get(INTERESTED));
        feedback.setRigorous(feedbackValues.get(RIGOROUS));
        feedback.setCourteous(feedbackValues.get(COURTEOUS));
        feedbackRepository.save(feedback);
    }

    public Feedback create(User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);

        return feedback;
    }
}
