package edu.platform.repo;

import edu.platform.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {

    User findUserByLogin(String login);

    List<User> findAll();

    List<User> findAllByLevel(int level);

}
