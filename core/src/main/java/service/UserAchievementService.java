package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.UserAchievement;
import models.Achievement;
import models.User;
import repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserAchievementService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAchievementService.class);

    private static final String POINTS = "points";

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementService achievementService;

    public void createOrUpdate(User user, JsonNode achievementJson) {
        if (achievementJson.isEmpty()) {
            LOG.error("Empty achievement");
            return;
        }

        Achievement achievement = achievementService.getOrCreate(achievementJson);
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);

        int points = achievementJson.get(POINTS).asInt();
        UserAchievement userAchievement = userAchievementOpt.orElseGet(() -> create(user, achievement));
        userAchievement.setPoints(points);
        userAchievementRepository.save(userAchievement);
    }

    private UserAchievement create(User user, Achievement achievement) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);

        return userAchievement;
    }
}
