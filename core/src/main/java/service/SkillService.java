package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Skill;
import repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SkillService {

    private static final Logger LOG = LoggerFactory.getLogger(SkillService.class);

    private static final String CODE = "code";
    private static final String TYPE = "type";

    private final SkillRepository skillRepository;

    public Skill getOrCreate(JsonNode skillJson) {
        if (skillJson.isEmpty()) {
            LOG.error("Empty skill");
            return null;
        }

        String id = skillJson.get(CODE).asText();
        String name = skillJson.get(TYPE).asText();
        Optional<Skill> skillOpt = skillRepository.findByName(name);

        return skillOpt.orElseGet(() -> create(id, name));
    }

    private Skill create(String id, String name) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setName(name);
        skillRepository.save(skill);

        return skill;
    }
}
