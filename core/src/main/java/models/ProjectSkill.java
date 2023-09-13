package models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "project_skills")
public class ProjectSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int points;

    @ManyToOne
    private Skill skill;
}
