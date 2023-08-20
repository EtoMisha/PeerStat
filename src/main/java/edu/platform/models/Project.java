package edu.platform.models;

import edu.platform.constants.EntityType;
import edu.platform.constants.ProjectType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "projects")
public class Project {

    @Id
    private Long entityId;
    private int nodeId;
    private String nodeCode;
    private Boolean isMandatory;
    private int courseId;
    private String projectName;
    private int points;
    private int duration;
    private String executionConditions;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    @OneToMany
    @JoinColumn(name = "parent_project_id")
    private List<Project> nextProjects;

    @OneToMany
    @JoinColumn(name = "project_id")
    private List<ProjectSkill> projectSkills;

    @OneToMany(mappedBy = "project")
    private List<UserProject> userProjectList;
}
