package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@Table(name = "campuses")
public class Campus {

    @Id
    private String schoolId;
    private String name;
    private String campusName;
    private String wavePrefix;
    private String fullLogin;
    private String login;
    private String password;

    @Column(columnDefinition = "TEXT")
    private String cookie;

    @ToString.Exclude
    @OneToMany(mappedBy = "campus")//, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<User> usersList;

    public Campus(String schoolId) {
        this.schoolId = schoolId;
    }

    public Campus() {}
}
