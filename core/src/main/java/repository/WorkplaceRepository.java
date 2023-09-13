package repository;

import models.Cluster;
import models.Workplace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkplaceRepository extends CrudRepository<Workplace, Long> {

    List<Workplace> findAll();
    Optional<Workplace> findByUserId(String userId);
    Optional<Workplace> findByClusterAndRowAndNumber(Cluster cluster, String row, int number);
}
