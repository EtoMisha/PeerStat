package edu.platform.connections;

import edu.platform.models.Achievement;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_achievements")
public class UserAchievement {

    @Id
    private Long id;
    private int points;

    @ManyToOne
    private Achievement achievement;
}
