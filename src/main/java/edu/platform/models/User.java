package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String login;

    private String userId;
    private String studentId;
    private String schoolId;
    private String email;

    private int level;
    private int xp;
    private int leftBorder;
    private int rightBorder;

    private String eduForm;
    private int waveId;
    private String waveName;
    private String bootcampId;
    private String bootcampName;
    private String coalitionName;

    private boolean isActive;
    private boolean isGraduate;

    private int peerPoints;
    private int codeReviewPoints;
    private int coins;

    @Column(columnDefinition = "TEXT")
    private String xpHistory;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserProject> userProjectList;

    public User(String login) {
        this.login = login;
    }

    public User() {}
}
