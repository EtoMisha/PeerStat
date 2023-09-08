package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.Project;
import constants.ProjectState;
import models.User;
import models.UserProject;
import repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(UserProjectService.class);

    private static final String GOAL_STATUS = "goalStatus";
    private static final String FINAL_PERCENTAGE = "finalPercentage";
    private static final List<ProjectState> ACTIVE_PROJECT_STATES = List.of(
            ProjectState.FAILED,
            ProjectState.COMPLETED,
            ProjectState.IN_PROGRESS,
            ProjectState.WAITING_FOR_START,
            ProjectState.READY_TO_START,
            ProjectState.P2P_EVALUATIONS
    );

    private final UserProjectRepository userProjectRepository;
    private final ProjectService projectService;

    public List<User> getProjectUsers(long projectId) {
        return userProjectRepository.findByProjectId(projectId).stream()
                .filter(this::isProjectStateActive)
                .map(userProjectMapper::getProjectUserView)
                .collect(Collectors.toList());
    }

    private boolean isProjectStateActive(UserProject up) {
        return up.getProjectState() != null && ACTIVE_PROJECT_STATES.contains(up.getProjectState());
    }

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
}
