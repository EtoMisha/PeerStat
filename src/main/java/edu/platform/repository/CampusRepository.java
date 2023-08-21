package edu.platform.repository;

import edu.platform.models.Campus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CampusRepository extends CrudRepository<Campus, String> {
    List<Campus> findAll();
}
