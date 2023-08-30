package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.GraphQLConstants;
import edu.platform.models.Campus;
import edu.platform.models.Cluster;
import edu.platform.repository.ClusterRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClusterService {
    private final ClusterRepository clusterRepository;
    private final WorkplaceService workplaceService;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);

    public void updateClusters(Campus campus, JsonNode clustersJson) {
        JsonNode buildingsList = clustersJson.at(GraphQLConstants.PATH_BUILDINGS);
        for (JsonNode building : buildingsList) {
            building.get(GraphQLConstants.CLASSROOMS).forEach(json -> createOrUpdate(campus, json));
        }
    }

    private void createOrUpdate(Campus campus, JsonNode clusterJson) {
        if (clusterJson.isEmpty()) {
            LOG.error("Empty cluster");
            return;
        }

        long id = clusterJson.get(GraphQLConstants.CLUSTER_ID).asLong();
        Optional<Cluster> clusterOpt = clusterRepository.findById(id);

        Cluster cluster = clusterOpt.orElseGet(Cluster::new);
        cluster.setId(id);
        cluster.setName(clusterJson.get(GraphQLConstants.CLUSTER_NAME).asText());
        cluster.setCapacity(clusterJson.get(GraphQLConstants.CAPACITY).asInt());
        cluster.setCampus(campus);

        clusterRepository.save(cluster);
    }

    public List<Cluster> getCampusClusters(Campus campus) {
        return clusterRepository.findClustersByCampus(campus);
    }
}
