package edu.platform.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.platform.models.RequestBody;
import edu.platform.models.User;
import edu.platform.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static edu.platform.parser.GraphQLConstants.*;

@Component
public class Parser {

    private UserRepo userRepo;

    @Value("${parser.usersList}")
    private String usersList;

    @Value("${parser.schoolId}")
    private String schoolId;

    @Value("${parser.cookie}")
    private String cookie;

    private final HttpHeaders headers = new HttpHeaders();
    private final ObjectMapper MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL = "https://edu.21-school.ru/services/graphql";

    public Parser() {}

    @Autowired
    public void setRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    private void initHeaders() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie);
        headers.add("schoolId", schoolId);
        headers.add("authority", "edu.21-school.ru");
    }

    public void initUsers(){
        initHeaders();
        System.out.println("[initUsers] headers " + headers);

        Scanner scanner;
        try {
            scanner = new Scanner(new File(usersList));
            while (scanner.hasNext()) {
                String login = scanner.nextLine();
                parseUser(login);
            }

        } catch (FileNotFoundException e) {
            System.out.println("[initUsers] ERROR " + e.getMessage());
        }
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
        } catch (IOException e) {
            System.out.println("[parseUser] ERROR " + e.getMessage());
        }
    }

    public void testInit(){
        initHeaders();
        System.out.println("[testInit] headers " + headers);

        String login = "fbeatris";
        parseUser(login);
    }

    public void updateUsers() {
        List<User> usersList = userRepo.findAll();
        for (User user : usersList) {
            try {
                setCredentials(user);
                setPersonalInfo(user);
                setXpHistory(user);
                setProject(user);

                updateUser(user);
                System.out.println("[updateUsers] user " + user.getLogin() + " ok");

            } catch (IOException e) {
                System.out.println("[updateUsers] ERROR " + e.getMessage());
            }
        }
    }

    private void setCredentials(User user) throws IOException {
        JsonNode credentialsInfo = sendRequest(RequestBody.getCredentialInfo(user));
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

    private void setPersonalInfo(User user) throws IOException {
        JsonNode personalInfo = sendRequest(RequestBody.getPersonalInfo(user));
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

    private void setCoalitionInfo(User user) throws IOException {
        JsonNode coalitionInfo = sendRequest(RequestBody.getCoalitionInfo(user));
        String coalitionName = null;
        if (coalitionInfo != null) {
            if (coalitionInfo.get(STUDENT) != null
                    && coalitionInfo.get(STUDENT).get(TOURNAMENT) != null
                    && coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER) != null
                    && coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION) != null
                    && coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME) != null){
                coalitionName = coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME).asText();
            }
        }
        user.setCoalitionName((coalitionName != null && !coalitionName.isEmpty()) ? coalitionName : "No Coalition");
    }

    private void setStageInfo(User user) throws IOException {
        JsonNode stageInfo = sendRequest(RequestBody.getStageInfo(user));
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

    private void setXpHistory(User user) throws IOException {
        JsonNode xpHistoryInfo = sendRequest(RequestBody.getXpHistory(user));
        JsonNode historyList = xpHistoryInfo.get(STUDENT).get(XP_HISTORY).get(HISTORY);

        ArrayNode historyData = MAPPER.createArrayNode();
        for (JsonNode history : historyList) {
            ObjectNode objectHistory = history.deepCopy();
            objectHistory.remove(TYPENAME);
            historyData.add(objectHistory);
        }

        user.setXpHistory(historyData.toString());
    }

    private void setProject(User user) throws IOException {
        JsonNode projectsInfo = sendRequest(RequestBody.getProjects(user));
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

    private JsonNode sendRequest(String requestBody) throws IOException {
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        String responseStr = restTemplate.postForObject(URL, request, String.class);
        return MAPPER.readTree(responseStr).get(DATA);
    }

    private void saveUser(User user) {
        userRepo.save(user);
    }

    private void updateUser(User user) {
        userRepo.save(user);
    }

    public String getUsersList() {
        return usersList;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getCookie() {
        return cookie;
    }


}
