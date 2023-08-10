package edu.platform.connections;

import edu.platform.models.Skill;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "project_skills")
public class ProjectSkill {

    @Id
    private Long id;
    private int points;

    @ManyToOne
    private Skill skill;
}
