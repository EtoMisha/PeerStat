package edu.platform.repository;

import edu.platform.models.Achievement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AchievementRepository extends CrudRepository<Achievement, Long> {
    Optional<Achievement> findByName(String name);
}
