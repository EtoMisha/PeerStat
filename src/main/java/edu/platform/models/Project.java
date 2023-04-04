package edu.platform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Project {
    @Id
    private Long id;
    private int goalId;
    private int projectId;
    private String code;
    private String parentCode;
    private String name;
    private boolean isCommand;
    private boolean isMandatory;
    private int points;
    private int duration;

    @OneToOne(fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private UserProject userProject;
}
