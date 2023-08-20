package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.models.Project;
import edu.platform.constants.ProjectState;
import edu.platform.models.User;
import edu.platform.models.UserProject;
import edu.platform.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class UserProjectService {

    private final UserProjectRepository userProjectRepository;
    private final ProjectService projectService;

    private static final Logger LOG = LoggerFactory.getLogger(UserProjectService.class);

    private static final List<ProjectState> ACTIVE_PROJECT_STATES = List.of(
            ProjectState.FAILED,
            ProjectState.COMPLETED,
            ProjectState.IN_PROGRESS,
            ProjectState.WAITING_FOR_START,
            ProjectState.READY_TO_START,
            ProjectState.P2P_EVALUATIONS
    );

    public void createOrUpdate(User user, JsonNode projectJson) {
        if (projectJson.isEmpty()) {
            LOG.error("Empty project");
            return;
        }

        Project project = projectService.getOrCreate(projectJson);
        Optional<UserProject> userProjectOpt = userProjectRepository.findByUserAndProject(user, project);
        UserProject userProject = userProjectOpt.orElseGet(() -> create(user, project));

        ProjectState state = ProjectState.valueOf(projectJson.get(GOAL_STATUS).asText());
        int score = projectJson.get(FINAL_PERCENTAGE).asInt();
        userProject.setProjectState(state);
        userProject.setScore(score);
        userProjectRepository.save(userProject);
    }

    private UserProject create(User user, Project project) {
        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);
        return userProject;
    }

//    public List<String> getCurrentUserProjects(User user) {
//        return userProjectRepository.findByUser(user).stream()
//                .filter(userProject -> userProject.getProjectState().equals(ProjectState.IN_PROGRESS))
//                .map(UserProject::getProject)
//                .map(Project::getProjectName)
//                .collect(Collectors.toList());
//    }
//
//    public List<ProjectUserView> getProjectUsersList(long projectId) {
//        return userProjectRepository.findByProjectId(projectId).stream()
//                .filter(this::isProjectStateActive)
//                .map(userProjectMapper::getProjectUserView)
//                .collect(Collectors.toList());
//    }
//
//    public void createAndSaveGoal(User user, Project project, JsonNode userProjectJson) {
//        UserProject userProject = create(user, project);
//        ProjectState state = ProjectState.valueOf(userProjectJson.get(GOAL_STATUS).asText());
//        int score = userProjectJson.get(FINAL_PERCENTAGE).asInt();
//        userProject.setProjectState(state);
//        userProject.setScore(score);
//        userProjectRepository.save(userProject);
//    }
//
//    public void createAndSaveCourse(User user, Project project, JsonNode userProjectJson) {
//        UserProject userProject = create(user, project);
//        ProjectState state = ProjectState.valueOf(userProjectJson.get(PROJECT_STATE).asText());
//        userProject.setProjectState(state);
//        userProjectRepository.save(userProject);
//    }
//
//    private UserProject create(User user, Project project) {
//        UserProject userProject = new UserProject();
//        userProject.setId(new UserProjectKey(user.getLogin(), project.getId()));
//        userProject.setUser(user);
//        userProject.setProject(project);
//        return userProject;
//    }
//
//    private boolean isProjectStateActive(UserProject up) {
//        return up.getProjectState() != null && ACTIVE_PROJECT_STATES.contains(up.getProjectState());
//    }
}
