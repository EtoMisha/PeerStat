package edu.platform.repository;

import edu.platform.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {

    List<User> findUsersByCampusSchoolId(String schoolId);



    Page<User> findUsersByCampusId(Long campusId, Pageable pageable);
    List<User> findByCampusId(Long campusId);

//    User findUserByLogin(String login);
//    List<User> findUsersByCampusSchoolId(String schoolId);
//    List<User> findUsersByCampusName(String campusName);
//    List<User> findAll();
//    List<User> findByOrderByXpDesc();
//    List<User> findAllByLevel(int level);
}
