package repository;

import models.UserSkill;
import models.Skill;
import models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserSkillRepository extends CrudRepository<UserSkill, Long> {
    List<UserSkill> findUserSkillsByUser(User user);

    Optional<UserSkill> findByUserAndSkill(User user, Skill skill);
}
