package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Cluster;
import edu.platform.models.User;
import edu.platform.models.Workplace;
import edu.platform.repository.WorkplaceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class WorkplaceService {
    private final WorkplaceRepository workplaceRepository;
    private final UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(WorkplaceService.class);

    public void updateWorkplaces(Cluster cluster, JsonNode clusterPlan) {
        JsonNode workplacesList = clusterPlan.at(GraphQLConstants.PATH_CLUSTER_PLAN);
        Map<String, Workplace> workplacesMap = new HashMap<>();
        for (JsonNode workPlaceJson : workplacesList) {
            try {
                Workplace workplace = create(cluster, workPlaceJson);
                workplacesMap.put(workplace.getId(), workplace);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        List<Workplace> currentWorkplaces = workplaceRepository.findAll();
        for (Workplace currentWorkplace : currentWorkplaces) {
            String id = currentWorkplace.getId();
            if (workplacesMap.containsKey(id)) {
                workplaceRepository.save(workplacesMap.get(id));
            } else {
                workplaceRepository.delete(currentWorkplace);
            }
        }

    }

    private Workplace create(Cluster cluster, JsonNode workplaceJson) throws Exception {
        String row = workplaceJson.get(GraphQLConstants.ROW).asText();
        int number = workplaceJson.get(GraphQLConstants.NUMBER).asInt();
        String userId = workplaceJson.at(GraphQLConstants.PATH_WORKPLACE_USER_ID).asText();

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            Workplace workplace = new Workplace();
            workplace.setId(cluster.getName() + "-" + row + number);
            workplace.setUser(userOpt.get());
            workplace.setCluster(cluster);
            workplace.setNumber(number);
            workplace.setRow(row);
            return workplace;
        } else {
            throw new Exception("No such user " + userId);
        }
    }

}
