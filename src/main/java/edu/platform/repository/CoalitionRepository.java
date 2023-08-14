package edu.platform.repository;

import edu.platform.models.Coalition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoalitionRepository extends CrudRepository<Coalition, Long> {

}
