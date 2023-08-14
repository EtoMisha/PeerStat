package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;

    public void createAndSave(JsonNode json) {

    }
}
