package edu.platform.repository;

import edu.platform.models.User;
import edu.platform.models.XpGain;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XpGainRepository extends CrudRepository<XpGain, Long> {
    List<XpGain> findXpGainsByUser(User user);
}
