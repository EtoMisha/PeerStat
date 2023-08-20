package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.models.UserSkill;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Skill;
import edu.platform.models.User;
import edu.platform.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSkillService {

    private static final Logger LOG = LoggerFactory.getLogger(UserSkillService.class);

    private final UserSkillRepository userSkillRepository;
    private final SkillService skillService;

    public List<UserSkill> getUserSkills(User user) {
        return userSkillRepository.findUserSkillsByUser(user);
    }

    public void createOrUpdate(User user, JsonNode skillJson) {
        if (skillJson.isEmpty()) {
            LOG.error("Empty skill");
            return;
        }

        Skill skill = skillService.getOrCreate(skillJson);
        Optional<UserSkill> userSkillOpt = userSkillRepository.findByUserAndSkill(user, skill);

        int points = skillJson.get(GraphQLConstants.TOTAL_POWER).asInt();
        UserSkill userSkill = userSkillOpt.orElseGet(() -> create(user, skill));
        userSkill.setPoints(points);
        userSkillRepository.save(userSkill);
    }

    public UserSkill create(User user, Skill skill) {
        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        return userSkill;
    }
}
