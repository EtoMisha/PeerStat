package models;

import constants.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;
    private String studentId;
    private String login;
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

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private int peerPoints;
    private int codeReviewPoints;
    private int coins;

    private double logTimeWeek;
    private double logTimeMonth;

    @OneToMany(mappedBy = "user")
    private List<UserProject> userProjectList;

    @ManyToOne
    private Campus campus;

    @ManyToOne
    private Coalition coalition;

    @OneToOne
    private Workplace workplace;

    @OneToOne
    private Feedback feedbacks;

    @OneToMany
    private List<UserAchievement> achievements;

    @OneToMany
    private List<XpGain> xpGains;

    public User() {}
}
