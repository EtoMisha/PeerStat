package edu.platform.repository;

import edu.platform.models.Skill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends CrudRepository<Skill, Long> {

    Optional<Skill> findByName(String name);

}
