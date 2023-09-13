package repository;

import models.Coalition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoalitionRepository extends CrudRepository<Coalition, Long> {
    Optional<Coalition> findByName(String name);
}
