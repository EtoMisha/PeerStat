package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Campus;
import edu.platform.models.Coalition;
import edu.platform.repository.CoalitionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CoalitionService {
    private final CoalitionRepository coalitionRepository;

    private static final Logger LOG = LoggerFactory.getLogger(CoalitionService.class);

    public Coalition getOrCreate(Campus campus, JsonNode coalitionJson) {
        if (coalitionJson.isEmpty()) {
            LOG.error("Empty coalition");
            return null;
        }

        String name = coalitionJson.at(GraphQLConstants.PATH_COALITION_NAME).asText();
        Optional<Coalition> coalitionOpt = coalitionRepository.findByName(name);

        return coalitionOpt.orElseGet(() -> create(name, campus));
    }

    public Coalition create(String name, Campus campus) {
        Coalition coalition = new Coalition();
        coalition.setName(name);
        coalition.setCampus(campus);
        coalitionRepository.save(coalition);

        return coalition;
    }



}
