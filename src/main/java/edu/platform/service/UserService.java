package edu.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.platform.View.UserView;
import edu.platform.models.ProjectStatus;
import edu.platform.models.User;
import edu.platform.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String LOGIN = "login";
    private static final String CAMPUS = "campus";
    private static final String COALITION = "coalition";
    private static final String WAVE = "wave";
    private static final String BOOTCAMP = "bootcamp";
    private static final String LEVEL = "level";
    private static final String XP = "xp";
    private static final String PEER_POINTS = "peerPoints";
    private static final String CODE_REVIEW_POINTS = "codeReviewPoints";
    private static final String COINS = "coins";
    private static final String DIFF_MONTH = "diffMonth";
    private static final String DIFF_3_MONTH = "diff3month";
    private static final String CURRENT_PROJECT = "currentProject";
    private static final String GOAL_STATUS = "goalStatus";
    private static final String PROJECT_NAME = "name";
    private static final String AWARD_DATE = "awardDate";
    private static final String XP_VALUE = "expValue";

    private static final Map<String, String> campusNames = Map.of("6bfe3c56-0211-4fe1-9e59-51616caac4dd", "Москва");

    private UserRepo userRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public void setFormatter(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserView> getAllUsers() {
        List<User> userList = userRepo.findByOrderByXpDesc();
        return userList.stream().map(UserView::new).toList();
    }

//
//    public String getAllUsersInfo() {
//        List<User> usersList = userRepo.findAll();
//        ArrayNode resultUsersArr = mapper.createArrayNode();
//        for (User user : usersList) {
//            resultUsersArr.add(getUserJson(user));
//        }
//        JsonNode result = mapper.createObjectNode().set("data", resultUsersArr);
//        return result.toString();
//    }
//
//    public String getUserInfo(String login) {
//        User user = userRepo.findUserByLogin(login);
//        JsonNode result = mapper.createObjectNode().set("data", getUserJson(user));
//        return result.toString();
//    }
//
//    public String getTestInfo() {
//        List<User> usersList = userRepo.findAllByLevel(10);
//        ArrayNode resultUsersArr = mapper.createArrayNode();
//        for (User user : usersList) {
//            resultUsersArr.add(getUserJson(user));
//        }
//        JsonNode result = mapper.createObjectNode().set("data", resultUsersArr);
//        return result.toString();
//    }
//
//    private ObjectNode getUserJson(User user) {
//        ObjectNode userJson = mapper.createObjectNode();
//        userJson.put(LOGIN, user.getLogin());
//        userJson.put(CAMPUS, campusNames.get(user.getSchoolId()));
//        userJson.put(COALITION, user.getCoalitionName());
//        userJson.put(WAVE, user.getWaveName());
//        userJson.put(BOOTCAMP, user.getBootcampName());
//        userJson.put(LEVEL, user.getLevel());
//        userJson.put(XP, user.getXp());
//        userJson.put(PEER_POINTS, user.getPeerPoints());
//        userJson.put(CODE_REVIEW_POINTS, user.getCodeReviewPoints());
//        userJson.put(COINS, user.getCoins());
//        userJson.put(DIFF_MONTH, getXpDiff(user, 1));
//        userJson.put(DIFF_3_MONTH, getXpDiff(user, 3));
//        userJson.put(CURRENT_PROJECT, getCurrentProject(user));
//        return userJson;
//    }
//
//    private String getCurrentProject(User user) {
//        String projectsStr = user.getProjects();
//        StringBuilder currentProjectSb = new StringBuilder();
//        try {
//            JsonNode projectListJson = mapper.readTree(projectsStr);
//            for (JsonNode projectJson : projectListJson) {
//                String projectStatus = projectJson.get(GOAL_STATUS).asText();
//                if (ProjectStatus.IN_PROGRESS.toString().equals(projectStatus)) {
//                    currentProjectSb.append(projectJson.get(PROJECT_NAME)).append(" ");
//                }
//            }
//        } catch (JsonProcessingException e) {
//            System.out.println("[userService] getCurrentProject ERROR " + e.getMessage());
//        }
//        return currentProjectSb.toString();
//    }
//
//    private int getXpDiff(User user, int noOfMonths) {
//        LocalDate minusMonth = LocalDate.now().minusMonths(noOfMonths);
//        int diff = 0;
//        try {
//            JsonNode xpHistory = mapper.readTree(user.getXpHistory());
//            int minXpValue = user.getXp();
//            for (JsonNode row : xpHistory) {
//                LocalDate date = LocalDate.parse(row.get(AWARD_DATE).asText());
//                int xpValue = row.get(XP_VALUE).asInt();
//                if (!date.isBefore(minusMonth) && xpValue < minXpValue) {
//                    minXpValue = xpValue;
//                }
//            }
//            diff = user.getXp() - minXpValue;
//        } catch (JsonProcessingException e) {
//            System.out.println("[userService] getMonthDiff ERROR " + e.getMessage());
//        }
//        return diff;
//    }
}
