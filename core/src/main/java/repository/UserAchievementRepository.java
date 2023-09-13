package repository;

import models.UserAchievement;
import models.Achievement;
import models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserAchievementRepository extends CrudRepository<UserAchievement, Long> {
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
}
