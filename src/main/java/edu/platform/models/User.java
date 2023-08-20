package edu.platform.models;

import edu.platform.constants.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private String login;

    private String userId;
    private String studentId;
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

    @Column(columnDefinition = "TEXT")
    private String xpHistory;

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
    @JoinColumn(name = "user_id")
    private List<UserAchievement> achievements;

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<XpGain> xpGains;

    public User() {}
}
