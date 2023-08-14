package edu.platform.repository;

import edu.platform.models.XpGain;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XpGainRepository extends CrudRepository<XpGain, Long> {

}
