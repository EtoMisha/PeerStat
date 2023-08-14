package edu.platform.repository;

import edu.platform.models.Cluster;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterRepository extends CrudRepository<Cluster, Long> {
    List<Cluster> findClustersByCampusId(Long campusId);

}
