package edu.platform.repository;

import edu.platform.models.Project;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    List<Project> findAll();

    Optional<Project> findByEntityId(int goalId);
}
