package models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "campuses")
public class Campus {

    @Id
    private String id;
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
    private List<Event> events;

    @OneToMany
    private List<Notification> notifications;

    public Campus(String id) {
        this.id = id;
    }

    public Campus() {}
}
