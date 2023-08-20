package edu.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.Achievement;
import edu.platform.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class AchievementService {

    private static final Logger LOG = LoggerFactory.getLogger(CoalitionService.class);

    private final AchievementRepository achievementRepository;

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final TypeReference<Map<String, String>> TYPE_REFERENCE_STRING_MAP = new TypeReference<Map<String, String>> () {};

    public Achievement getOrCreate(JsonNode achievementJson) {
        if (achievementJson.isEmpty()) {
            LOG.error("Empty achievement");
            return null;
        }

        Map<String, String> achievementMap = MAPPER.convertValue(achievementJson.at(BADGE), TYPE_REFERENCE_STRING_MAP);
        String name = achievementMap.get(NAME);
        String avatarUrl = achievementMap.get(AVATAR_URL);
        Optional<Achievement> achievementOpt = achievementRepository.findByName(name);

        return achievementOpt.orElseGet(() -> create(name, avatarUrl));
    }

    public Achievement create(String name, String avatarUrl) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setAvatarUrl(avatarUrl);
        achievementRepository.save(achievement);

        return achievement;
    }
}
