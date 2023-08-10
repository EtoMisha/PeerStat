package edu.platform.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.*;
import edu.platform.service.CampusService;
import edu.platform.service.ProjectService;
import edu.platform.service.UserService;
import edu.platform.service.UserProjectService;
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
    private static final String AUTHORITY = "edu.21-school.ru";
    private static final String GRAPHQL_URL = "https://edu.21-school.ru/services/graphql";

    private static final String LAST_UPDATE_PROPERTIES_FILE = "last-update.properties";
    private static final String LAST_UPDATE_TIME = "task-update.time";

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CampusService campusService;
    private final UserService userService;
    private final ProjectService projectService;
    private final UserProjectService userProjectService;
    private final CookiesGrabber cookiesGrabber;

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

    public void parseGraphInfo(Campus campus) throws IOException {
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
//        String cookies = cookiesGrabber.getCookies(campus.getUserFullLogin(), campus.getUserPassword());
        String cookies = "_ga_94PX1KP3QL=GS1.1.1692571942.70.1.1692572668.0.0.0; _ga=GA1.1.758073578.1682197028; SI=52ea1b06-998d-484f-ba5d-1e727579c799; tokenId=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE2OTI2MDc5NDEsImlhdCI6MTY5MjU3MjY2NywiYXV0aF90aW1lIjoxNjkyNTcxOTQxLCJqdGkiOiI4ODJmZmY3NS0yNWRlLTQ0YjktYjM5NC01OTcwNTZmNGJmMDkiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDhjMjY0MTgtOGY5NS00N2Y4LWIwZTEtNmM5ZWVjZWI3NDY5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjgxZTA2ZjdkLTZmMWItNGQ5ZC1iNzU3LWZjZDk2YjQ2NDMxMiIsInNlc3Npb25fc3RhdGUiOiI2YzY0MjIxZi1jMjIxLTQwOGQtOWRlZC0yMTMxNmNhZTAyNzAiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6ImI1NjkzZjhlLTQxOGUtNDg3ZC05MmIxLTI2OGFlMmM1ODQ1ZiIsIm5hbWUiOiJGZXJuYW5kYSBCZWF0cmlzIiwiYXV0aF90eXBlX2NvZGUiOiJkZWZhdWx0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiZmJlYXRyaXNAc3R1ZGVudC4yMS1zY2hvb2wucnUiLCJnaXZlbl9uYW1lIjoiRmVybmFuZGEiLCJmYW1pbHlfbmFtZSI6IkJlYXRyaXMiLCJlbWFpbCI6ImZiZWF0cmlzQHN0dWRlbnQuMjEtc2Nob29sLnJ1In0.KmwND1l_9YrM1SlN7P4p3AZ3Vfw7g1V5QCrbe8NSRP2BAhOWsE8pDCSoLj58LxWAAjoWwMrmvbf_mHcHqi83DMDE_A5-2Qaa8Vlof4Jmj7Vl4VYhGmeP4oCO6QrkIk1wT5sgr4cfv3Gmp2be_zckLA_SrZ2m_bFUBgcvGv1nGA0130uj0VIjgfGhGwGSBhKqDHxolPBtsFvdQ7NtQHMUKvRQGXhG7RsMygp7i4ijF50kqnyrhYOpIUw0YLggRTHmnCMhqCEmnkoL1qyY87wusToOyZ5XnCXqkHZmHDBicSfoRSmqeigiRuUs6LEBSd6rLIZo2CuXjCGzH-mD7iiDiw; localeCode=en_EN";
        campusService.setCookies(campus, cookies);
    }

    public void updateWorkplaces(Campus campus) {
        //TODO
    }

}
