package edu.platform.repository;

import edu.platform.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    User findUserByLogin(String login);
    List<User> findUsersByCampusSchoolId(String schoolId);
    List<User> findUsersByCampusName(String campusName);
    List<User> findAll();
    List<User> findByOrderByXpDesc();
    List<User> findAllByLevel(int level);
}
