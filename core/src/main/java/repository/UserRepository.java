package repository;

import models.Campus;
import models.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {

    List<User> findByCampus(Campus campus);
    List<User> findByCampusName(String campusName);
    Optional<User> findById(String id);
    Optional<User> findByLogin(String login);
    void save(User user);

}
