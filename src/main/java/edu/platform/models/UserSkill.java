package edu.platform.models;

import edu.platform.models.Skill;
import edu.platform.models.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int points;

    @ManyToOne
    private Skill skill;

    @ManyToOne
    private User user;
}
