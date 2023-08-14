package edu.platform.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.constants.EntityType;
import edu.platform.constants.ProjectState;
import edu.platform.models.*;
import edu.platform.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

import static edu.platform.constants.GraphQLConstants.*;

@Component
@RequiredArgsConstructor
public class Parser {

    private static final int SEARCH_LIMIT = 25;
    private static final String LAST_UPDATE_PROPERTIES_FILE = "last-update.properties";
    private static final String LAST_UPDATE_TIME = "task-update.time";

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UserService userService;
    private final ProjectService projectService;
    private final UserProjectService userProjectService;
    private final AchievementService achievementService;
    private final LoginService loginService;

    public void initUsers(Campus campus){
        LOG.info("Init users begin by login " + campus.getUserFullLogin());

        List<String> currentUsersList = userService.findUsersBySchoolId(campus.getSchoolId()).stream()
                .map(User::getLogin).toList();

        int offset = 0;
        try {
            List<String> tempLoginsList = getSearchResults(campus, offset);
            while (!tempLoginsList.isEmpty()) {
                tempLoginsList.stream()
                        .filter(Predicate.not(currentUsersList::contains))
                        .forEach(login -> parseNewUser(campus, login));

                offset += SEARCH_LIMIT;
                tempLoginsList = getSearchResults(campus, offset);
            }

            setLastUpdateTime();
            LOG.info("Init users end");

        } catch (IOException e) {
            LOG.error("Parser error " + e.getMessage());
        }
    }

    private void parseNewUser(Campus campus, String login) {
//        try {
            User user = new User(login);
            user.setCampus(campus);
            setCredentials(user);
            setPersonalInfo(user);

            if (CORE_PROGRAM.equals(user.getEduForm())) {
                setCoalitionInfo(user);
                setStageInfo(user);
                setXpHistory(user);
                setAchievements(user);
                userService.save(user);

                setUserProjects(user);
                setUserProjectsFromGraph(user);

                System.out.println("[parseUser] user done " + login);
            } else {
                System.out.println("[parseUser] user skipped " + login);
            }
//        } catch (Exception e) {
//            System.out.println("[parseUser] ERROR " + login + " " + e.getMessage());
//        }
    }

    public void updateUsers(Campus campus) {
        System.out.println("[parser updateUsers] updateUsers by login " + campus.getUserFullLogin());

        List<User> usersList = userService.findUsersBySchoolId(campus.getSchoolId());
        for (User user : usersList) {
            try {
                setCredentials(user);
                setPersonalInfo(user);
                setXpHistory(user);
                userService.save(user);

                setUserProjects(user);
                setUserProjectsFromGraph(user);

//                System.out.println("[parser updateUsers] user " + user.getLogin() + " ok");
            } catch (Exception e) {
                System.out.println("[parser updateUsers] ERROR " + user.getLogin() + " " + e.getMessage());
            }
        }

        System.out.println("[parser updateUsers] done " + LocalDateTime.now());
        setLastUpdateTime();
    }

    public void testInit(Campus campus){
        System.out.println("[parser testInit] testInit by login " + campus.getUserFullLogin());

        String login = campus.getUserLogin();
        parseNewUser(campus, login);
    }

    private void setCredentials(User user) throws JsonProcessingException {
        JsonNode credentials = getResponseJson(user.getCampus(), RequestBody.getCredentialInfo(user));
        userService.setCredentials(user, credentials);
    }

    private void setAchievements(User user) throws IOException {
        JsonNode response = getResponseJson(user.getCampus(), RequestBody.getAchievements(user));

    }

    private void setPersonalInfo(User user) throws IOException {
        userService.setPersonalInfo(user, getResponseJson(user.getCampus(), RequestBody.getPersonalInfo(user)));
    }

    private void setCoalitionInfo(User user) throws IOException {
        userService.setCoalitionInfo(user, getResponseJson(user.getCampus(), RequestBody.getCoalitionInfo(user)));
    }

    private void setStageInfo(User user) throws IOException {
        userService.setStageInfo(user, getResponseJson(user.getCampus(), RequestBody.getStageInfo(user)));
    }

    private void setXpHistory(User user) throws IOException {
        userService.setXpHistory(user, getResponseJson(user.getCampus(), RequestBody.getXpHistory(user)));
    }

    private void setUserProjectsFromGraph(User user) throws IOException {
        JsonNode graphInfo = getResponseJson(user.getCampus(), RequestBody.getGraphInfo(user));
        if (!graphInfo.isEmpty()) {
            JsonNode projectsListJson = graphInfo.get(STUDENT).get(BASIC_GRAPH).get(GRAPH_NODES);
            for (JsonNode projectJson : projectsListJson) {
                EntityType entityType = EntityType.valueOf(projectJson.get(ENTITY_TYPE).asText());
                if (entityType.equals(EntityType.COURSE)) {
                    String stateStr = projectJson.get(COURSE).get(PROJECT_STATE).asText();
                    ProjectState projectState = stateStr == null ? null : ProjectState.valueOf(stateStr);
                    if (projectState != null && !ProjectState.LOCKED.equals(projectState)) {
                        Long projectId = projectJson.get(ENTITY_ID).asLong();
                        Optional<Project> projectOpt = projectService.findById(projectId);
                        if (projectOpt.isPresent() ) {
                            userProjectService.createAndSaveCourse(user, projectOpt.get(), projectJson.get(COURSE));
                        } else {
                            System.out.println("[PARSER] ERROR Project not found, id" + projectId);
                        }
                    }
                }
            }
        }
    }

    private void setUserProjects(User user) throws IOException {
        JsonNode userProjectInfo = getResponseJson(user.getCampus(), RequestBody.getUserProjects(user));
        if (!userProjectInfo.isEmpty()) {
            JsonNode userProjectListJson = userProjectInfo.get(SCHOOL_21).get(STUDENT_PROJECT);
            for (JsonNode userProjectJson : userProjectListJson) {
                ProjectState projectState = ProjectState.valueOf(userProjectJson.get(GOAL_STATUS).asText());
                if (!ProjectState.UNAVAILABLE.equals(projectState)) {
                    Long projectId = Long.parseLong(userProjectJson.get(GOAL_ID).asText());
                    Optional<Project> projectOpt = projectService.findById(projectId);
                    if (projectOpt.isPresent()) {
                        userProjectService.createAndSaveGoal(user, projectOpt.get(), userProjectJson);
                    } else {
                        System.out.println("[PARSER] ERROR Project not found, id" + projectId);
                    }
                }
            }
        }
    }

    public void parseGraphInfo(Campus campus) throws IOException {
        System.out.println("[parser parseGraphInfo] begin");

        String userLogin = campus.getUserLogin();
        User user = userService.findUserByLogin(userLogin);
        if (user == null) {
            user = new User(userLogin);
            user.setCampus(campus);
            setCredentials(user);
        }

        JsonNode graphInfo = getResponseJson(campus, RequestBody.getGraphInfo(user));

        if (!graphInfo.isEmpty()) {
            JsonNode projectsListJson = graphInfo.get(STUDENT).get(BASIC_GRAPH).get(GRAPH_NODES);
            projectsListJson.forEach(projectJson -> projectService.save(projectJson));
        }
        System.out.println("[parser parseGraphInfo] done");
    }

    private List<String> getSearchResults(Campus campus, int offset) throws IOException {
        List<String> loginList = new ArrayList<>();
        JsonNode searchInfo = getResponseJson(campus, RequestBody.getSearchResults(SEARCH_LIMIT, offset));
        if (!searchInfo.isEmpty()) {
            JsonNode profiles = searchInfo.get(SCHOOL_21).get(SEARCH_BY_TEXT).get(PROFILES).get(PROFILES);
            for (JsonNode profile : profiles) {
                String fullLogin = profile.get(LOGIN).asText();
                String login = fullLogin.contains("@") ? fullLogin.substring(0, fullLogin.indexOf("@")) : fullLogin;
                loginList.add(login);
            }
        }
        LOG.info("Search result: " + loginList);
        return loginList;
    }

    private JsonNode getResponseJson(Campus campus, String requestBody) throws JsonProcessingException {
        String response = loginService.sendRequest(campus, requestBody);
        return MAPPER.readTree(response).get(DATA);
    }

    public String getLastUpdateTime() {
        String lastUpdateTime = "";

        try {
            Properties props = new Properties();
            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
            p.load(props, new FileInputStream(LAST_UPDATE_PROPERTIES_FILE));

            lastUpdateTime = props.getProperty(LAST_UPDATE_TIME);

        } catch (IOException e) {
            System.out.println("[PARSER] ERROR " + e.getMessage());
        }

        return lastUpdateTime;
    }

    public void setLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String lastUpdateTime = LocalDateTime.now().format(formatter);

        try {
            Properties props = new Properties();
            props.setProperty(LAST_UPDATE_TIME, lastUpdateTime);

            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
            p.store(props, new FileOutputStream(LAST_UPDATE_PROPERTIES_FILE), "parser last update time");

        } catch (IOException e) {
            LOG.error("Set last update time error " + e.getMessage());
        }
    }

}
