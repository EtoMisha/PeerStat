package models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double punctual;
    private double interested;
    private double rigorous;
    private double courteous;

    @OneToOne
    private User user;
}
