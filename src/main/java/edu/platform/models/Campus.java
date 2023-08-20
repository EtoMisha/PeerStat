package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "campuses")
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String schoolId;
    private String name;
    private String campusName;
    private String wavePrefix;

    private String userFullLogin;
    private String userLogin;
    private String userPassword;

    @Column(columnDefinition = "TEXT")
    private String cookie;

    @OneToMany(mappedBy = "campus")
    private List<Cluster> clusters;

    @OneToMany(mappedBy = "campus")
    private List<Coalition> coalitions;

    @OneToMany(mappedBy = "campus")
    private List<User> users;

    @OneToMany
    @JoinColumn(name = "campus_id")
    private List<Event> events;

    @OneToMany
    @JoinColumn(name = "campus_id")
    private List<Notification> notifications;

    public Campus(String schoolId) {
        this.schoolId = schoolId;
    }

    public Campus() {}
}
