package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Campus;
import models.Coalition;
import repository.CoalitionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CoalitionService {

    private static final Logger LOG = LoggerFactory.getLogger(CoalitionService.class);

    private static final String PATH_COALITION_NAME = "/getUserTournamentWidget/coalitionMember/coalition/name";

    private final CoalitionRepository coalitionRepository;

    public Coalition getOrCreate(Campus campus, JsonNode coalitionJson) {
        if (coalitionJson.isEmpty()) {
            LOG.error("Empty coalition");
            return null;
        }

        String name = coalitionJson.at(PATH_COALITION_NAME).asText();
        Optional<Coalition> coalitionOpt = coalitionRepository.findByName(name);

        return coalitionOpt.orElseGet(() -> create(name, campus));
    }

    public Coalition create(String name, Campus campus) {
        Coalition coalition = new Coalition();
        coalition.setId(name);
        coalition.setCampus(campus);
        coalitionRepository.save(coalition);

        return coalition;
    }
}
