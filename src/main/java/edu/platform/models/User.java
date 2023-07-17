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
    private String coalitionName;

    private boolean isActive;
    private boolean isGraduate;

    private int peerPoints;
    private int codeReviewPoints;
    private int coins;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String xpHistory;

    @ToString.Exclude
    @OneToMany(mappedBy = "user")//, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserProject> userProjectList;

    @ManyToOne
    @JoinColumn(name="schoolId", nullable = false)
    private Campus campus;

    public User(String login) {
        this.login = login;
    }

    public User() {}
}
