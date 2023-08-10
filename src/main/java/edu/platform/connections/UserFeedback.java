package edu.platform.connections;

import edu.platform.models.Feedback;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_feedbacks")
public class UserFeedback {

    @Id
    private Long id;
    private double points;

    @ManyToOne
    private Feedback feedback;
}
