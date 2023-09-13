package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Achievement;
import repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AchievementService {

    private static final Logger LOG = LoggerFactory.getLogger(AchievementService.class);

    private static final String BADGE = "badge";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String AVATAR_URL = "avatarUrl";

    private final AchievementRepository achievementRepository;

    public Achievement getOrCreate(JsonNode achievementJson) {
        if (achievementJson.isEmpty()) {
            LOG.error("Empty achievement");
            return null;
        }

        JsonNode achievement = achievementJson.get(BADGE);
        Long id = achievement.get(ID).asLong();
        String name = achievement.get(NAME).asText();
        String avatarUrl = achievement.get(AVATAR_URL).asText();
        Optional<Achievement> achievementOpt = achievementRepository.findById(id);

        return achievementOpt.orElseGet(() -> create(id, name, avatarUrl));
    }

    private Achievement create(Long id, String name, String avatarUrl) {
        Achievement achievement = new Achievement();
        achievement.setId(id);
        achievement.setName(name);
        achievement.setAvatarUrl(avatarUrl);
        achievementRepository.save(achievement);

        return achievement;
    }
}
