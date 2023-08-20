package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.models.UserAchievement;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Achievement;
import edu.platform.models.User;
import edu.platform.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserAchievementService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAchievementService.class);

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementService achievementService;

    public void createOrUpdate(User user, JsonNode achievementJson) {
        if (achievementJson.isEmpty()) {
            LOG.error("Empty achievement");
            return;
        }

        Achievement achievement = achievementService.getOrCreate(achievementJson);
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);

        int points = achievementJson.get(GraphQLConstants.TOTAL_POWER).asInt();
        UserAchievement userAchievement = userAchievementOpt.orElseGet(() -> create(user, achievement));
        userAchievement.setPoints(points);
        userAchievementRepository.save(userAchievement);
    }

    public UserAchievement create(User user, Achievement achievement) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        return userAchievement;
    }
}
