package edu.platform.repository;

import edu.platform.models.Project;
import edu.platform.models.User;
import edu.platform.models.UserProject;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProjectRepository extends CrudRepository<UserProject, Long> {

    List<UserProject> findByUser(User user);
    Optional<UserProject> findByUserAndProject(User user, Project project);
}
