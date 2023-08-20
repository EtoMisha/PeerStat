package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Skill;
import edu.platform.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SkillService {

    private static final Logger LOG = LoggerFactory.getLogger(SkillService.class);

    private final SkillRepository skillRepository;

    public Skill getOrCreate(JsonNode skillJson) {
        if (skillJson.isEmpty()) {
            LOG.error("Empty skill");
            return null;
        }

        String id = skillJson.get(GraphQLConstants.CODE).asText();
        String name = skillJson.get(GraphQLConstants.TYPE).asText();
        Optional<Skill> skillOpt = skillRepository.findByName(name);

        return skillOpt.orElseGet(() -> create(id, name));
    }

    public Skill create(String id, String name) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setName(name);
        skillRepository.save(skill);

        return skill;
    }

}
