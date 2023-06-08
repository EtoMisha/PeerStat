package edu.platform.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.platform.models.Campus;
import edu.platform.models.User;
import edu.platform.repo.UserRepository;
import edu.platform.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import static edu.platform.parser.GraphQLConstants.*;

@Component
public class Parser {

    private static final String URL = "https://edu.21-school.ru/services/graphql";
    private static final String AUTHORITY = "edu.21-school.ru";
    private static final int SEARCH_LIMIT = 25;
    private static final String LAST_UPDATE_PROPERTIES_FILE = "last-update.properties";
    private static final String LAST_UPDATE_TIME = "task-update.time";

    private UserRepository userRepository;
    private LoginService loginService;

    private final HttpHeaders headers = new HttpHeaders();
    private final ObjectMapper MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public void setRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    public void login(Campus campus) {
        System.out.println("[parser login] start login ");
        String cookie = loginService.getCookies(campus.getLogin(), campus.getPassword());
        headers.remove("Cookie");
        headers.remove("schoolId");
        headers.add("Cookie", cookie);
        headers.add("schoolId", campus.getSchoolId());
        headers.add("authority", AUTHORITY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        System.out.println("[parser login] ok");
    }

    public void initUsers(Campus campus){
        System.out.println("[PARSER] initUsers by login " + campus.getLogin());
        login(campus);
        System.out.println("[initUsers] headers " + headers);
        List<String> currentUsersList = userRepository.findUsersBySchoolId(campus.getSchoolId()).stream()
                .map(User::getLogin).toList();

        int offset = 0;
        try {
            List<String> tempLoginsList = getSearchResults(offset);
            while (!tempLoginsList.isEmpty()) {
                tempLoginsList.stream()
                        .filter(Predicate.not(currentUsersList::contains))
                        .forEach(this::parseUser);

                offset += SEARCH_LIMIT;
                tempLoginsList = getSearchResults(offset);
            }

            setLastUpdateTime();

        } catch (IOException e) {
            System.out.println("[initUsers] ERROR offset " + offset + " " + e.getMessage());
        }
    }

    public void testInit(Campus campus){
        System.out.println("[PARSER] testInit by login " + campus.getLogin());
        login(campus);
        System.out.println("[testInit] headers " + headers);

        String login = campus.getLogin().substring(0, campus.getLogin().indexOf("@student"));
        parseUser(login);
    }

    public void updateUsers(Campus campus) {
        System.out.println("[PARSER] updateUsers by login " + campus.getLogin());
        login(campus);
        System.out.println("[updateUsers] headers " + headers);

        List<User> usersList = userRepository.findUsersBySchoolId(campus.getSchoolId());
        for (User user : usersList) {
            try {
                setCredentials(user);
                setPersonalInfo(user);
                setXpHistory(user);
                setProject(user);

                updateUser(user);
                System.out.println("[updateUsers] user " + user.getLogin() + " ok");

            } catch (Exception e) {
                System.out.println("[updateUsers] ERROR " + user.getLogin() + " " + e.getMessage());
            }
        }

        System.out.println("[updateUsers] done " + LocalDateTime.now());
        setLastUpdateTime();
    }

    private void parseUser(String login) {
        try {
            User user = new User(login);
            setCredentials(user);
            setPersonalInfo(user);
            if (CORE_PROGRAM.equals(user.getEduForm())) {
                setCoalitionInfo(user);
                setStageInfo(user);
                setXpHistory(user);
                setProject(user);
                saveUser(user);
                System.out.println("[parseUser] user done " + login);
            } else {
                System.out.println("[parseUser] user skipped " + login);
            }
        } catch (Exception e) {
            System.out.println("[parseUser] ERROR " + login + " " + e.getMessage());
        }
    }

    private void setCredentials(User user) throws IOException {
        JsonNode credentialsInfo = sendRequest(RequestBody.getCredentialInfo(user));
        if (!credentialsInfo.isEmpty()) {
            JsonNode studentJson = credentialsInfo.get(SCHOOL_21).get(GET_STUDENT_BY_LOGIN);
            String studentId = studentJson.get(STUDENT_ID).asText();
            String userId = studentJson.get(USER_ID).asText();
            String schoolId = studentJson.get(SCHOOL_ID).asText();
            boolean isActive = studentJson.get(IS_ACTIVE).asBoolean();
            boolean isGraduate = studentJson.get(IS_GRADUATE).asBoolean();

            user.setStudentId(studentId);
            user.setUserId(userId);
            user.setSchoolId(schoolId);
            user.setActive(isActive);
            user.setGraduate(isGraduate);
        }
    }

    private void setPersonalInfo(User user) throws IOException {
        JsonNode personalInfo = sendRequest(RequestBody.getPersonalInfo(user));
        if (!personalInfo.isEmpty()) {
            JsonNode student = personalInfo.get(STUDENT);
            JsonNode stageInfo = student.get(STAGE_INFO);

            int waveId = stageInfo.get(WAVE_ID) != null ? stageInfo.get(WAVE_ID).asInt() : 0;
            String waveName = stageInfo.get(WAVE_NAME) != null ? stageInfo.get(WAVE_NAME).asText() : "No wave";
            String eduForm = stageInfo.get(EDU_FORM) != null ? stageInfo.get(EDU_FORM).asText() : "No edu form";

            JsonNode xpInfo = student.get(XP_INFO);
            int xpValue = xpInfo.get(VALUE).asInt();
            JsonNode level = xpInfo.get(LEVEL);
            int levelCode = level.get(LEVEL_CODE).asInt();
            int leftBorder = level.get(RANGE).get(LEFT_BORDER).asInt();
            int rightBorder = level.get(RANGE).get(RIGHT_BORDER).asInt();
            int peerPoints = xpInfo.get(PEER_POINTS).asInt();
            int coinsCount = xpInfo.get(COINS_COUNT).asInt();
            int codeReviewPoints = xpInfo.get(CODE_REVIEW_POINTS).asInt();

            String email = student.get(EMAIL).asText();

            user.setWaveId(waveId);
            user.setWaveName(waveName);
            user.setEduForm(eduForm);
            user.setXp(xpValue);
            user.setLevel(levelCode);
            user.setLeftBorder(leftBorder);
            user.setRightBorder(rightBorder);
            user.setPeerPoints(peerPoints);
            user.setCoins(coinsCount);
            user.setCodeReviewPoints(codeReviewPoints);
            user.setEmail(email);
        }
    }

    private void setCoalitionInfo(User user) throws IOException {
        JsonNode coalitionInfo = sendRequest(RequestBody.getCoalitionInfo(user));
        if (!coalitionInfo.isEmpty()) {
            String coalitionName = null;
            if (coalitionInfo.get(STUDENT) != null
                    && coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME) != null) {
                coalitionName = coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME).asText();
            }
            user.setCoalitionName((coalitionName != null && !coalitionName.isEmpty()) ? coalitionName : "No Coalition");
        }
    }

    private void setStageInfo(User user) throws IOException {
        JsonNode stageInfo = sendRequest(RequestBody.getStageInfo(user));
        if (!stageInfo.isEmpty()) {
            JsonNode school21 = stageInfo.get(SCHOOL_21);
            JsonNode stageGroupsArr = school21.get(LOAD_STAGE_GROUPS);

            String bootcampId = null;
            String bootcampName = null;
            for (JsonNode stageGroup : stageGroupsArr) {
                String eduForm = stageGroup.get(STAGE_GROUPS).get(EDU_FORM).asText();
                if (SURVIVAL_CAMP.equals(eduForm)) {
                    bootcampId = stageGroup.get(STAGE_GROUPS).get(WAVE_ID).asText();
                    bootcampName = stageGroup.get(STAGE_GROUPS).get(WAVE_NAME).asText();
                }
            }

            if (bootcampId == null) {
                bootcampId = "No bootcamp";
                bootcampName = "No bootcamp";
            }

            user.setBootcampId(bootcampId);
            user.setBootcampName(bootcampName);
        }
    }

    private void setXpHistory(User user) throws IOException {
        JsonNode xpHistoryInfo = sendRequest(RequestBody.getXpHistory(user));
        if (!xpHistoryInfo.isEmpty()) {
            JsonNode historyList = xpHistoryInfo.get(STUDENT).get(XP_HISTORY).get(HISTORY);

            ArrayNode historyData = MAPPER.createArrayNode();
            for (JsonNode history : historyList) {
                ObjectNode objectHistory = history.deepCopy();
                objectHistory.remove(TYPENAME);
                historyData.add(objectHistory);
            }

            user.setXpHistory(historyData.toString());
        }
    }

    private void setProject(User user) throws IOException {
        JsonNode projectsInfo = sendRequest(RequestBody.getProjects(user));
        if (!projectsInfo.isEmpty()) {
            JsonNode projectsList = projectsInfo.get(SCHOOL_21).get(STUDENT_PROJECT);

            ArrayNode projectsData = MAPPER.createArrayNode();
            for (JsonNode project : projectsList) {
                ObjectNode projectObj = project.deepCopy();
                if (!STATUS_UNAVAILABLE.equals(projectObj.get(GOAL_STATUS).asText())) {
                    projectObj.remove(TYPENAME);
                    projectsData.add(projectObj);
                }
            }

            user.setProjects(projectsData.toString());
        }
    }

    private List<String> getSearchResults(int offset) throws IOException {
        List<String> loginList = new ArrayList<>();
        JsonNode searchInfo = sendRequest(RequestBody.getSearchResults(SEARCH_LIMIT, offset));
        if (!searchInfo.isEmpty()) {
            JsonNode profiles = searchInfo.get(SCHOOL_21).get(SEARCH_BY_TEXT).get(PROFILES).get(PROFILES);
            for (JsonNode profile : profiles) {
                String fullLogin = profile.get(LOGIN).asText();
                String login = fullLogin.contains("@") ? fullLogin.substring(0, fullLogin.indexOf("@")) : fullLogin;
                loginList.add(login);
            }
        }
        System.out.println("[getSearchResults] ok, logins: " + loginList);
        return loginList;
    }

    private JsonNode sendRequest(String requestBody) throws IOException {
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        String responseStr = "";
        try {
            responseStr = restTemplate.postForObject(URL, request, String.class);
        } catch (RestClientException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return MAPPER.readTree(responseStr).get(DATA);
    }

    private void saveUser(User user) {
        userRepository.save(user);
    }

    private void updateUser(User user) {
        userRepository.save(user);
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

        } catch (Exception e ) {
            System.out.println("[PARSER] ERROR " + e.getMessage());
        }
    }


}
