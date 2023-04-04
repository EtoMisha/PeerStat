package edu.platform.View;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.ProjectStatus;
import edu.platform.models.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Data
@Component
public class UserView {
    private String login;
    private String campus;
    private String coalition;
    private String wave;
    private String bootcamp;
    private int level;
    private int xp;
    private int peerPoints;
    private int codeReviewPoints;
    private int coins;
    private int diff;
    private int diff3;
    private String currentProject;

    private static final Map<String, String> campusNames = Map.of("6bfe3c56-0211-4fe1-9e59-51616caac4dd", "Москва");
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String GOAL_STATUS = "goalStatus";
    private static final String PROJECT_NAME = "name";
    private static final String AWARD_DATE = "awardDate";
    private static final String XP_VALUE = "expValue";
    private static final String NO_BOOTCAMP = "";

    public UserView() {
    }

    public UserView(User user) {
        this.login = user.getLogin();
        this.campus = campusNames.get(user.getSchoolId());
        this.coalition = user.getCoalitionName();
        this.wave = user.getWaveName();
        this.bootcamp = user.getBootcampName().equals(NO_BOOTCAMP) ? "" : user.getBootcampName();
        this.level = user.getLevel();
        this.xp = user.getXp();
        this.peerPoints = user.getPeerPoints();
        this.codeReviewPoints = user.getCodeReviewPoints();
        this.coins = user.getCoins();
        this.diff = getXpDiff(user, 1);
        this.diff3 = getXpDiff(user, 3);
        this.currentProject = getCurrentProject(user);
    }
//
//    private String getProfileLink(User user) {
//        return String.format("<a href=\"https://edu.21-school.ru/profile/%s@student.21-school.ru\">", user.getLogin())
//                + user.getLogin() + "</a>";
//    }

    private String getCurrentProject(User user) {
        String projectsStr = user.getProjects();
        StringBuilder currentProjectSb = new StringBuilder();
        try {
            JsonNode projectListJson = mapper.readTree(projectsStr);
            for (JsonNode projectJson : projectListJson) {
                String projectStatus = projectJson.get(GOAL_STATUS).asText();
                if (ProjectStatus.IN_PROGRESS.toString().equals(projectStatus)) {
                    currentProjectSb.append(projectJson.get(PROJECT_NAME).asText()).append(", ");
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println("[userService] getCurrentProject ERROR " + e.getMessage());
        }
        String result = currentProjectSb.toString();
        return result.length() > 2 ? result.substring(0, result.length() - 2) : result;
    }

    private int getXpDiff(User user, int noOfMonths) {
        LocalDate minusMonth = LocalDate.now().minusMonths(noOfMonths);
        int diff = 0;
        try {
            JsonNode xpHistory = mapper.readTree(user.getXpHistory());
            int minXpValue = user.getXp();
            for (JsonNode row : xpHistory) {
                LocalDate date = LocalDate.parse(row.get(AWARD_DATE).asText());
                int xpValue = row.get(XP_VALUE).asInt();
                if (!date.isBefore(minusMonth) && xpValue < minXpValue) {
                    minXpValue = xpValue;
                }
            }
            diff = user.getXp() - minXpValue;
        } catch (JsonProcessingException e) {
            System.out.println("[userService] getMonthDiff ERROR " + e.getMessage());
        }
        return diff;
    }
}
