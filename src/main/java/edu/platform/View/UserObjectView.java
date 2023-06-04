package edu.platform.View;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Data
@Component
public class UserObjectView {
    private String login;
    private String email;
    private String campus;
    private String coalition;
    private String wave;
    private String platformClass;
    private String bootcamp;
    private int level;
    private int xp;
    private int peerPoints;
    private int codeReviewPoints;
    private int coins;
    private int diff;
    private int diff3;
    private String currentProject;

    private static final String SCHOOL_ID_MSK = "6bfe3c56-0211-4fe1-9e59-51616caac4dd";
    private static final String SCHOOL_ID_KZN = "kzn";
    private static final String SCHOOL_ID_NSK = "46e7d965-21e9-4936-bea9-f5ea0d1fddf2";

    private static final Map<String, String> CAMPUS_NAMES_MAP = Map.of(
            SCHOOL_ID_MSK, "Москва",
            SCHOOL_ID_KZN, "Казань",
            SCHOOL_ID_NSK, "Новосибирск");
    private static final Map<String, String> WAVE_PREFIX_MAP = Map.of(
            SCHOOL_ID_MSK, "O_msk_",
            SCHOOL_ID_KZN, "O_kzn_",
            SCHOOL_ID_NSK, "O_nsk_");
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String GOAL_STATUS = "goalStatus";
    private static final String PROJECT_NAME = "name";
    private static final String AWARD_DATE = "awardDate";
    private static final String XP_VALUE = "expValue";
    private static final String NO_BOOTCAMP = "No bootcamp";
    private static final String WAVE_INTRA = "Intra";
    private static final String PROJECT_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String WAVE_UNKNOWN = "???";

    public UserObjectView() {
    }

    public UserObjectView(User user) {
        this.login = user.getLogin() + (user.isGraduate()
                ? " (alumni)"
                : (user.isActive() ? "" : " (deactivated)"));
        this.email = user.getEmail();
        this.campus = CAMPUS_NAMES_MAP.get(user.getSchoolId());
        this.coalition = user.getCoalitionName();
        this.wave = getRealWave(user);
        this.platformClass = user.getWaveName();
        this.bootcamp = user.getBootcampName();//.equals(NO_BOOTCAMP) ? "" : user.getBootcampName();
        this.level = user.getLevel();
        this.xp = user.getXp();
        this.peerPoints = user.getPeerPoints();
        this.codeReviewPoints = user.getCodeReviewPoints();
        this.coins = user.getCoins();
        this.diff = getXpDiff(user, 1);
        this.diff3 = getXpDiff(user, 3);
        this.currentProject = getCurrentProject(user);
    }

    private String getCurrentProject(User user) {
        String projectsStr = user.getProjects();
        StringBuilder currentProjectSb = new StringBuilder();
        try {
            JsonNode projectListJson = mapper.readTree(projectsStr);
            for (JsonNode projectJson : projectListJson) {
                String projectStatus = projectJson.get(GOAL_STATUS).asText();
                if (PROJECT_STATUS_IN_PROGRESS.equals(projectStatus)) {
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

    private String getRealWave(User user) {
        String platformClass = user.getWaveName();
        String wavePrefix = WAVE_PREFIX_MAP.get(user.getSchoolId());

        if (platformClass.startsWith(wavePrefix) && platformClass.length() == 13) {
            platformClass = platformClass.substring(
                    wavePrefix.length(),
                    platformClass.indexOf("_", wavePrefix.length()));
        } else if (platformClass.startsWith(wavePrefix)){
            platformClass = WAVE_INTRA;
        } else {
            platformClass = WAVE_UNKNOWN;
        }
        return platformClass;
    }
}
