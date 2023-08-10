package edu.platform.connections;

import edu.platform.models.Skill;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    private Long id;
    private int points;

    @ManyToOne
    private Skill skill;
}
