package service;

import com.fasterxml.jackson.databind.JsonNode;
import constants.EntityType;
import constants.ProjectType;
import models.Project;
import repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private static final String PATH_GRAPH = "/student/getBasisGraph/graphNodes";
    private static final String GRAPH_NODE_ID = "graphNodeId";
    private static final String NODE_CODE = "nodeCode";
    private static final String ENTITY_ID = "entityId";
    private static final String ENTITY_TYPE = "entityType";
    private static final String GOAL_TYPE = "goalExecutionType";
    private static final String IS_MANDATORY = "isMandatory";
    private static final String COURSE_TYPE = "courseType";
    private static final String COURSE_ID = "localCourseId";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String PROJECT_POINTS = "projectPoints";
    private static final String DURATION = "duration";
    private static final String GOAL_ID = "goalId";
    private static final String PATH_PROJECT_INFO = "/student/getModuleById";
    private static final String STUDY_MODULE = "studyModule";
    private static final String IDEA = "idea";
    private static final String GOAL_POINTS = "goalPoint";
    private static final String NAME = "name";

    private final ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> findById(long projectId) {
        return projectRepository.findById(projectId);
    }

    public void updateGraph(JsonNode graphJson) {
        if (graphJson.isEmpty()) {
            LOG.error("Empty graph");
            return;
        }

        JsonNode projectsListJson = graphJson.at(PATH_GRAPH);
        projectsListJson.forEach(this::createOrUpdate);
    }

    private void createOrUpdate(JsonNode projectJson) {
        Project project = new Project();
        project.setId(projectJson.get(ENTITY_ID).asLong());
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
            project.setIsMandatory(details.get(IS_MANDATORY).asBoolean());
        } else if (entityType.equals(EntityType.COURSE)) {
            project.setProjectType(ProjectType.valueOf(details.get(COURSE_TYPE).asText()));
            project.setCourseId(details.get(COURSE_ID).asInt());
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
        project.setId(projectJson.get(GOAL_ID).asLong());
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
}
