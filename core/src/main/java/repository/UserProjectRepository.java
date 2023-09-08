package repository;

import models.Project;
import models.User;
import models.UserProject;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProjectRepository extends CrudRepository<UserProject, Long> {

    List<UserProject> findByUser(User user);
    List<UserProject> findByProjectId(Long id);
    Optional<UserProject> findByUserAndProject(User user, Project project);
}
