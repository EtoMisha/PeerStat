package edu.platform.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.*;
import edu.platform.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
    private static final String GRAPHQL_URL = "https://edu.21-school.ru/services/graphql";

    private static final String LAST_UPDATE_PROPERTIES_FILE = "last-update.properties";
    private static final String LAST_UPDATE_TIME = "task-update.time";

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CookiesGrabber cookiesGrabber;
    private final CampusService campusService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ClusterService clusterService;
    private final WorkplaceService workplaceService;
    private final EventService eventService;

    public void parseCampusInfo(List<Campus> campusList) {
        try {
            for (Campus campus : campusList) {
                updateCookies(campus);
                parseClusters(campus);
                parseEvents(campus);
                parseWorkplaces(campus);
            }
            parseGraph(campusList.get(0));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initUsers(Campus campus) {
        LOG.info("Init users begin by login " + campus.getUserFullLogin());

        List<String> currentUsersList = userService.findUsersByCampus(campus).stream()
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
        try {
            User user = userService.create(campus, login);
            userService.setCredentials(user, getResponse(campus, Request.getCredentialInfo(user)));
            LOG.info("-- setCredentials ok");
            userService.setPersonalInfo(user, getResponse(campus, Request.getPersonalInfo(user)));
            LOG.info("-- setPersonalInfo ok");

            if (CORE_PROGRAM.equals(user.getEduForm())) {
                userService.save(user);
            } else {
                LOG.info("User skipped " + login);
                return;
            }

            userService.setIntensive(user, getResponse(campus, Request.getStageInfo(user)));
            LOG.info("-- setIntensive ok");
            userService.setCoalition(user, getResponse(campus, Request.getCredentialInfo(user)));
            LOG.info("-- setCoalition ok");

            updateDynamicUserData(user);

            LOG.info("User done " + login);

        } catch (Exception e) {
            LOG.error("Parse new user error " + login + " " + e.getMessage());
        }
    }

    public void testInit(Campus campus) {
        LOG.info("Test Init campus " + campus.getName());

        String login = campus.getUserLogin();
        parseNewUser(campus, login);
    }

    public void updateUsers(Campus campus) {
        LOG.info("Update users, campus " + campus.getName());

        userService.findUsersByCampus(campus).forEach(this::updateDynamicUserData);

        LOG.info("Update users done " + LocalDateTime.now());
        setLastUpdateTime();
    }

    private void updateDynamicUserData(User user) {
        Campus campus = user.getCampus();
        try {
            userService.setAchievements(user, getResponse(campus, Request.getAchievements(user)));
            LOG.info("-- setAchievements ok");
            userService.setSkills(user, getResponse(campus, Request.getUserSkills(user)));
            LOG.info("-- setSkills ok");
            userService.setXpGains(user, getResponse(campus, Request.getXpGains(user)));
            LOG.info("-- setXpGains ok");
            userService.setProjects(user, getResponse(campus, Request.getUserProjects(user)));
            LOG.info("-- setProjects ok");

            userService.save(user);

        } catch (Exception e) {
            LOG.error("Update user error " + user.getLogin() + " " + e.getMessage());
        }
    }

    public void updateProjects(Campus campus, Project project) {
        try {
            JsonNode projectInfoJson = getResponse(campus, Request.getProjectInfo(project.getEntityId()));
            projectService.updateProjectInfo(project, projectInfoJson);
        } catch (JsonProcessingException e) {
            LOG.error("Update project error " + e.getMessage());
        }
    }

//    private void setUserProjectsFromGraph(User user) throws IOException {
//        JsonNode graphInfo = getResponse(user.getCampus(), Request.getGraphInfo(user));
//        if (!graphInfo.isEmpty()) {
//            JsonNode projectsListJson = graphInfo.get(STUDENT).get(BASIC_GRAPH).get(GRAPH_NODES);
//            for (JsonNode projectJson : projectsListJson) {
//                EntityType entityType = EntityType.valueOf(projectJson.get(ENTITY_TYPE).asText());
//                if (entityType.equals(EntityType.COURSE)) {
//                    String stateStr = projectJson.get(COURSE).get(PROJECT_STATE).asText();
//                    ProjectState projectState = stateStr == null ? null : ProjectState.valueOf(stateStr);
//                    if (projectState != null && !ProjectState.LOCKED.equals(projectState)) {
//                        Long projectId = projectJson.get(ENTITY_ID).asLong();
//                        Optional<Project> projectOpt = projectService.findById(projectId);
//                        if (projectOpt.isPresent() ) {
//                            userProjectService.createAndSaveCourse(user, projectOpt.get(), projectJson.get(COURSE));
//                        } else {
//                            System.out.println("[PARSER] ERROR Project not found, id" + projectId);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//

    public void parseGraph(Campus campus) throws IOException {
        LOG.info("Parse graph begin");

        String userLogin = campus.getUserLogin();
        Optional<User> userOpt = userService.findByLogin(userLogin);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = userService.create(campus, campus.getUserLogin());
            parseNewUser(campus, campus.getUserLogin());
        }

        projectService.updateGraph(getResponse(campus, Request.getGraphInfo(user)));

        LOG.info("Parse graph done");
    }

    public void parseClusters(Campus campus) {
        try {
            clusterService.updateClusters(campus, getResponse(campus, Request.getClusters()));
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    public void parseWorkplaces(Campus campus) {
        try {
            List<Cluster> clustersList = clusterService.getCampusClusters(campus);
            for (Cluster cluster : clustersList) {
                JsonNode clusterPlan = getResponse(campus, Request.getClusterPlanInfo(cluster.getId()));
                workplaceService.updateWorkplaces(cluster, clusterPlan);
            }
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    public void parseEvents(Campus campus) {
        try {
            eventService.updateEvents(getResponse(campus, Request.getEvents()));
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    private List<String> getSearchResults(Campus campus, int offset) throws IOException {
        List<String> loginList = new ArrayList<>();
        JsonNode searchInfo = getResponse(campus, Request.getSearchResults(SEARCH_LIMIT, offset));
        if (!searchInfo.isEmpty()) {
            JsonNode profiles = searchInfo.at(PATH_SEARCH_RESULT);
            for (JsonNode profile : profiles) {
                String fullLogin = profile.get(LOGIN).asText();
                String login = fullLogin.contains("@") ? fullLogin.substring(0, fullLogin.indexOf("@")) : fullLogin;
                loginList.add(login);
            }
        }

        LOG.info("Search result: " + loginList);
        return loginList;
    }

    public String getLastUpdateTime() {
        String lastUpdateTime = "";

        try {
            Properties props = new Properties();
            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
            p.load(props, new FileInputStream(LAST_UPDATE_PROPERTIES_FILE));

            lastUpdateTime = props.getProperty(LAST_UPDATE_TIME);

        } catch (IOException e) {
            LOG.error("Get last update time error " + e.getMessage());
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

    private JsonNode getResponse(Campus campus, String requestBody) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", campus.getCookie());
        headers.set("schoolId", campus.getId());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(GRAPHQL_URL, HttpMethod.POST, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return MAPPER.readTree(response.getBody()).get(DATA);
        } else {
            LOG.error("Response code " + response.getStatusCode());
            throw new ResponseStatusException(response.getStatusCode());
        }
    }

    public void updateCookies(Campus campus) {
        String cookies = cookiesGrabber.getCookies(campus.getUserFullLogin(), campus.getUserPassword());
//        String cookies = "_ga_94PX1KP3QL=GS1.1.1692643557.73.1.1692643593.0.0.0; _ga=GA1.1.758073578.1682197028; SI=52ea1b06-998d-484f-ba5d-1e727579c799; tokenId=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE2OTI2Nzk1NTYsImlhdCI6MTY5MjY0MzU5MSwiYXV0aF90aW1lIjoxNjkyNjQzNTU2LCJqdGkiOiI3NmQ2MDRlNS1hZjk5LTQ5YjgtYTljOC0yODU0NWIzYmI5YjQiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDhjMjY0MTgt…XNAc3R1ZGVudC4yMS1zY2hvb2wucnUiLCJnaXZlbl9uYW1lIjoiRmVybmFuZGEiLCJmYW1pbHlfbmFtZSI6IkJlYXRyaXMiLCJlbWFpbCI6ImZiZWF0cmlzQHN0dWRlbnQuMjEtc2Nob29sLnJ1In0.HqEpPz-k6pCDMQbaz__5oiQrFoWC3IRME6VI4vlyvRB4OgLGcnW1p3KaozCkeBIjIok1ff0Jnd3hGtA3W4TEzwdUITWK4_XtC9YItoMXDrc3yAqeNWDVSOCbKQvKlzUkPFJGvYTaJrq6EsqhsjCCyUnqXvjt9R4V3_tG_tEssaF5dL8gQc9UbY8PCmvU6FZFLwFhAOZuBzVMRSEX1e9rZ6fBQkUuyXrJpFEH_bYu4_p79siMxKrf7n5-8-qJDpYPakKgXWUD8eNbCwxA4WrsK3N995jXWFIT8s33bXnd9sVg-aaZmJQ_oNH1HhgZW0JwmKm9mYmgVh_ql9qEkEUdvQ; localeCode=en_EN";
        campusService.setCookies(campus, cookies);
    }

}
