package edu.platform.service;

import edu.platform.models.Campus;
import edu.platform.models.Cluster;
import edu.platform.repository.ClusterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClusterService {
    private final ClusterRepository clusterRepository;

    public List<Cluster> getCampusClusters(Campus campus) {
        return clusterRepository.findClustersByCampus(campus);
    }
}
