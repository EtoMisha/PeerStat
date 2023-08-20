package edu.platform.repository;

import edu.platform.models.Feedback;
import edu.platform.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    Optional<Feedback> findByUser(User user);
}
