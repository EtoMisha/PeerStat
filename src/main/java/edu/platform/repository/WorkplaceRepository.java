package edu.platform.repository;

import edu.platform.models.Cluster;
import edu.platform.models.User;
import edu.platform.models.Workplace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WorkplaceRepository extends CrudRepository<Workplace, Long> {

    List<Workplace> findAll();
    Optional<Workplace> findByUserId(String userId);
    Optional<Workplace> findByClusterAndRowAndNumber(Cluster cluster, String row, int number);
}
