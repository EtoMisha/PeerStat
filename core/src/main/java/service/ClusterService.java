package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Campus;
import models.Cluster;
import repository.ClusterRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClusterService {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);

    private static final String PATH_BUILDINGS = "/student/getBuildings";
    private static final String CLASSROOMS = "classrooms";
    private static final String CLUSTER_ID = "id";
    private static final String CLUSTER_NAME = "number";
    private static final String CAPACITY = "capacity";

    private final ClusterRepository clusterRepository;

    public List<Cluster> getCampusClusters(Campus campus) {
        return clusterRepository.findClustersByCampus(campus);
    }

    public void updateClusters(Campus campus, JsonNode clustersJson) {
        if (clustersJson.isEmpty()) {
            LOG.error("Empty clusters");
            return;
        }

        JsonNode buildingsList = clustersJson.at(PATH_BUILDINGS);
        for (JsonNode building : buildingsList) {
            building.get(CLASSROOMS).forEach(json -> createOrUpdate(campus, json));
        }
    }

    private void createOrUpdate(Campus campus, JsonNode clusterJson) {
        long id = clusterJson.get(CLUSTER_ID).asLong();
        Optional<Cluster> clusterOpt = clusterRepository.findById(id);

        Cluster cluster = clusterOpt.orElseGet(Cluster::new);
        cluster.setId(id);
        cluster.setName(clusterJson.get(CLUSTER_NAME).asText());
        cluster.setCapacity(clusterJson.get(CAPACITY).asInt());
        cluster.setCampus(campus);
        clusterRepository.save(cluster);
    }
}
