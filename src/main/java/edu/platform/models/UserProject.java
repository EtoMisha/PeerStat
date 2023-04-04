package edu.platform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserProject {
    @Id
    private Long id;

    @OneToOne(mappedBy = "userProject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private Project project;
    private ProjectStatus status;
    private int result;
    private int xp;

    @ManyToOne(fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private User user;
}
