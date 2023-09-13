package models;

import constants.ProjectState;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_projects")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;
    private LocalDateTime finishDate;

    @Enumerated(EnumType.STRING)
    private ProjectState projectState;

    @ManyToOne
    private User user;

    @ManyToOne
    private Project project;
}
