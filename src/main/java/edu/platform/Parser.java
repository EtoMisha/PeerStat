package edu.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.platform.models.ProjectStatus;
import edu.platform.models.RequestBody;
import edu.platform.models.User;
import edu.platform.repo.UserRepo;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

@Component
public class Parser {

    private ApplicationContext context;

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

    private static final String DATA = "data";
    private static final String GET_STUDENT_BY_LOGIN = "getStudentByLogin";
    private static final String STUDENT_ID = "studentId";
    private static final String USER_ID = "userId";
    private static final String SCHOOL_ID = "schoolId";
    private static final String IS_ACTIVE = "isActive";
    private static final String IS_GRADUATE = "isGraduate";

    private static final String STUDENT = "student";
    private static final String STAGE_INFO = "getStageGroupS21PublicProfile";
    private static final String WAVE_ID = "waveId";
    private static final String WAVE_NAME = "waveName";
    private static final String EDU_FORM = "eduForm";

    private static final String XP_INFO = "getExperiencePublicProfile";
    private static final String VALUE = "value";
    private static final String LEVEL = "level";
    private static final String LEVEL_CODE = "levelCode";
    private static final String RANGE = "range";
    private static final String LEFT_BORDER = "leftBorder";
    private static final String RIGHT_BORDER = "rightBorder";
    private static final String PEER_POINTS = "cookiesCount";
    private static final String COINS_COUNT = "coinsCount";
    private static final String CODE_REVIEW_POINTS = "codeReviewPoints";
    private static final String EMAIL = "getEmailbyUserId";

    private static final String TOURNAMENT = "getUserTournamentWidget";
    private static final String MEMBER = "coalitionMember";
    private static final String COALITION = "coalition";
    private static final String NAME = "name";

    private static final String SCHOOL_21 = "school21";
    private static final String LOAD_STAGE_GROUPS = "loadStudentStageGroupsS21PublicProfile";
    private static final String STAGE_GROUPS = "stageGroupS21";
    private static final String SURVIVAL_CAMP = "Survival camp";
    private static final String XP_HISTORY = "getExperienceHistoryDate";
    private static final String HISTORY = "history";
    private static final String TYPENAME = "__typename";

    private static final String STUDENT_PROJECT = "getStudentProjectsForPublicProfileByStageGroup";
    private static final String GOAL_STATUS = "goalStatus";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String CORE_PROGRAM = "Core program";

    public Parser() {}

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    private void initHeaders() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie);
        headers.add("schoolId", schoolId);
        headers.add("authority", "edu.21-school.ru");
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

    public void initUsers(){
        initHeaders();
        System.out.println("[Headers] " + headers);

        Scanner scanner;
        try {
            scanner = new Scanner(new File(usersList));
            while (scanner.hasNext()) {
                String login = scanner.nextLine();
//            String login = "azraelna";
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
                        System.out.println("[Parser] user done " + login);
                    } else {
                        System.out.println("[Parser] user skipped " + login);
                    }
                } catch (IOException e) {
                    System.out.println("[Parser] ERROR " + e.getMessage());
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("[Parser] ERROR " + e.getMessage());
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
//            coalitionName = coalitionInfo.at("/" + STUDENT + "/" + TOURNAMENT + "/" + MEMBER + "/" + COALITION + "/" + NAME + "/").asText();
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
        UserRepo userRepo = context.getBean(UserRepo.class);
//        User foundUser = userRepo.findUserByLogin(user.getLogin());
//        if (foundUser == null) {
            userRepo.save(user);
//        } else {
//            userRepo.
//        }

    }

}
