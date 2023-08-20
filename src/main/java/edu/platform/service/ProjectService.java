package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.EntityType;
import edu.platform.constants.ProjectType;
import edu.platform.models.Project;
import edu.platform.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public void updateGraph(JsonNode graphJson) {
        JsonNode projectsListJson = graphJson.at(PATH_GRAPH);
        projectsListJson.forEach(this::createOrUpdate);
    }

    private void createOrUpdate(JsonNode projectJson) {
        Project project = new Project();
        project.setEntityId(projectJson.get(ENTITY_ID).asInt());
        project.setNodeId(projectJson.get(GRAPH_NODE_ID).asInt());
        project.setNodeCode(projectJson.get(NODE_CODE).asText());

        EntityType entityType = EntityType.valueOf(projectJson.get(ENTITY_TYPE).asText());
        project.setEntityType(entityType);

        JsonNode details = projectJson.get(entityType.name().toLowerCase());
        project.setProjectName(details.get(PROJECT_NAME).asText());
        project.setDescription(details.get(PROJECT_DESCRIPTION).asText());
        project.setPoints(details.get(PROJECT_POINTS).asInt());
        project.setDuration(details.get(DURATION).asInt());

        if (entityType.equals(EntityType.GOAL)) {
            project.setProjectType(ProjectType.valueOf(details.get(GOAL_TYPE).asText()));
            project.setIsMandatory(Boolean.valueOf(details.get(IS_MANDATORY).asText()));
        } else if (entityType.equals(EntityType.COURSE)) {
            project.setProjectType(ProjectType.valueOf(details.get(COURSE_TYPE).asText()));
            project.setCourseId(details.get(COURSE_ID).asInt());
            project.setIsMandatory(Boolean.valueOf(details.get(IS_MANDATORY).asText()));
        }

        projectRepository.save(project);
    }

    public Project getOrCreate(JsonNode projectJson) {
        if (projectJson.isEmpty()) {
            LOG.error("Empty project");
            return null;
        }

        int entityId = projectJson.get(GOAL_ID).asInt();
        Optional<Project> projectOpt = projectRepository.findByEntityId(entityId);

        return projectOpt.orElseGet(() -> create(projectJson));
    }

    private Project create(JsonNode projectJson) {
        Project project = new Project();
        project.setEntityId(projectJson.get(GOAL_ID).asInt());
        project.setProjectName(projectJson.get(NAME).asText());
        projectRepository.save(project);
        return project;
    }

    public void updateProjectInfo(Project project, JsonNode projectInfoJson) {
        JsonNode projectJson = projectInfoJson.at(PATH_PROJECT_INFO);
        project.setProjectType(ProjectType.valueOf(projectJson.get(GOAL_TYPE).asText()));

        JsonNode moduleJson = projectJson.get(STUDY_MODULE);
        project.setDescription(moduleJson.get(IDEA).asText());
        project.setDuration(moduleJson.get(DURATION).asInt());
        project.setPoints(moduleJson.get(GOAL_POINTS).asInt());

        projectRepository.save(project);
    }


//    public Optional<Project> findById(Long id) {
//        return projectRepository.findById(id);
//    }
//
//    public Optional<Project> findById(String id) {
//        return projectRepository.findById(Long.parseLong(id));
//    }
//
//    public ProjectView getProjectInfo(long id) {
//        Project project = projectRepository.findById(id).orElse(new Project());
//        return projectMapper.getProjectView(project);
//    }
//
//    public List<ProjectView> getProjectListForWeb() {
//        return projectRepository.findAll().stream()
//                .map(projectMapper::getProjectView)
//                .toList();
//    }
//
}
