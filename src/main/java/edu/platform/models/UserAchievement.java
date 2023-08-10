package edu.platform.models;

import edu.platform.models.Achievement;
import edu.platform.models.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_achievements")
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int points;

    @ManyToOne
    private Achievement achievement;

    @ManyToOne
    private User user;
}
