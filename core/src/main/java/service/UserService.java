package service;

import com.fasterxml.jackson.databind.JsonNode;
import constants.ProjectState;
import constants.UserStatus;
import models.*;
import repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private static final String NO_BOOTCAMP = "No bootcamp";
    private static final String NO_WAVE = "No wave";
    private static final String NO_EDU_FORM = "No edu form";

    private static final String PATH_STUDENT = "/school21/getStudentByLogin";
    private static final String STUDENT_ID = "studentId";
    private static final String USER_ID = "userId";
    private static final String IS_ACTIVE = "isActive";
    private static final String IS_GRADUATE = "isGraduate";
    private static final String STUDENT = "student";
    private static final String EMAIL = "getEmailbyUserId";

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

    private static final String PATH_STAGE_GROUPS = "/school21/loadStudentStageGroupsS21PublicProfile";
    private static final String STAGE_GROUPS = "stageGroupS21";
    private static final String SURVIVAL_CAMP = "Survival camp";

    private static final String PATH_FEEDBACK = "/getFeedbackStatisticsAverageScore/feedbackAverageScore";
    private static final String PATH_SKILLS = "/school21/getSoftSkillsByStudentId";
    private static final String PATH_ACHIEVEMENTS = "/student/getBadgesPublicProfile";
    private static final String PATH_USER_PROJECTS = "/school21/getStudentProjectsForPublicProfileByStageGroup";
    private static final String GOAL_STATUS = "goalStatus";

    private final UserRepository userRepository;
    private final CoalitionService coalitionService;
    private final UserAchievementService userAchievementService;
    private final UserSkillService userSkillService;
    private final UserProjectService userProjectService;
    private final FeedbackService feedbackService;
    private final XpGainService xpGainService;

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public List<User> findByCampus(Campus campus) {
        return userRepository.findByCampus(campus);
    }

    public List<User> findByCampusName(String campusName) {
        return userRepository.findByCampusName(campusName);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User create(Campus campus, String login) {
        User user = new User();
        user.setLogin(login);
        user.setCampus(campus);

        return user;
    }

    public void setCredentials(User user, JsonNode credentialsJson) {
        if (credentialsJson.isEmpty()) {
            LOG.error("Empty credentials, user " + user.getLogin());
            return;
        }

        JsonNode credentials = credentialsJson.at(PATH_STUDENT);
        user.setId(credentials.get(USER_ID).asText());
        user.setStudentId(credentials.get(STUDENT_ID).asText());

        UserStatus status;
        if (credentials.get(IS_GRADUATE).asBoolean()) {
            status = UserStatus.ALUMNI;
        } else if (credentials.get(IS_ACTIVE).asBoolean()) {
            status = UserStatus.STUDENT;
        } else {
            status = UserStatus.DEACTIVATED;
        }

        user.setStatus(status);
    }

    public void setPersonalInfo(User user, JsonNode personalInfoJson) {
        if (personalInfoJson.isEmpty()) {
            LOG.error("Empty personalInfo, user " + user.getLogin());
            return;
        }

        JsonNode student = personalInfoJson.get(STUDENT);
        JsonNode stageInfo = student.get(STAGE_INFO);
        JsonNode xpInfo = student.get(XP_INFO);
        JsonNode level = xpInfo.get(LEVEL);

        int waveId = stageInfo.get(WAVE_ID) != null ? stageInfo.get(WAVE_ID).asInt() : 0;
        String waveName = stageInfo.get(WAVE_NAME) != null ? stageInfo.get(WAVE_NAME).asText() : NO_WAVE;
        String eduForm = stageInfo.get(EDU_FORM) != null ? stageInfo.get(EDU_FORM).asText() : NO_EDU_FORM;

        user.setWaveId(waveId);
        user.setWaveName(waveName);
        user.setEduForm(eduForm);
        user.setEmail(student.get(EMAIL).asText());

        user.setXp(xpInfo.get(VALUE).asInt());
        user.setPeerPoints(xpInfo.get(PEER_POINTS).asInt());
        user.setCoins(xpInfo.get(COINS_COUNT).asInt());
        user.setCodeReviewPoints(xpInfo.get(CODE_REVIEW_POINTS).asInt());

        user.setLevel(level.get(LEVEL_CODE).asInt());
        user.setLeftBorder(level.get(RANGE).get(LEFT_BORDER).asInt());
        user.setRightBorder(level.get(RANGE).get(RIGHT_BORDER).asInt());

        userRepository.save(user);

        feedbackService.createOrUpdate(user, student.at(PATH_FEEDBACK));
    }

    public void setIntensive(User user, JsonNode intensiveJson) {
        if (intensiveJson.isEmpty()) {
            LOG.error("Empty intensive, user " + user.getLogin());
            return;
        }

        String bootcampId = NO_BOOTCAMP;
        String bootcampName = NO_BOOTCAMP;

        JsonNode stageGroupsArr = intensiveJson.at(PATH_STAGE_GROUPS);
        for (JsonNode stageGroup : stageGroupsArr) {
            JsonNode group = stageGroup.get(STAGE_GROUPS);
            String eduForm = group.get(EDU_FORM).asText();
            if (SURVIVAL_CAMP.equals(eduForm)) {
                bootcampId = group.get(WAVE_ID).asText();
                bootcampName = group.get(WAVE_NAME).asText();
            }
        }

        user.setBootcampId(bootcampId);
        user.setBootcampName(bootcampName);
    }

    public void setCoalition(User user, JsonNode coalitionJson) {
        if (coalitionJson.isEmpty()) {
            LOG.error("Empty coalition, user " + user.getLogin());
            return;
        }

        Coalition coalition = coalitionService.getOrCreate(user.getCampus(), coalitionJson);
        user.setCoalition(coalition);
    }

    public void setAchievements(User user, JsonNode achievementsJson) {
        if (achievementsJson.isEmpty()) {
            LOG.error("Empty achievements, user " + user.getLogin());
            return;
        }

        JsonNode achievementsArr = achievementsJson.at(PATH_ACHIEVEMENTS);
        for (JsonNode achievementJson : achievementsArr) {
            userAchievementService.createOrUpdate(user, achievementJson);
        }
    }

    public void setSkills(User user, JsonNode skillsJson) {
        if (skillsJson.isEmpty()) {
            LOG.error("Empty skills, user " + user.getLogin());
            return;
        }

        JsonNode skillsArr = skillsJson.at(PATH_SKILLS);
        for (JsonNode skillJson : skillsArr) {
            userSkillService.createOrUpdate(user, skillJson);
        }
    }

    public void setXpGains(User user, JsonNode xpGainsJson) {
        if (xpGainsJson.isEmpty()) {
            LOG.error("Empty xpGains, user " + user.getLogin());
            return;
        }

        xpGainService.createIfNotExist(user, xpGainsJson);
    }

    public void setProjects(User user, JsonNode projectsJson) {
        if (projectsJson.isEmpty()) {
            LOG.error("Empty projects, user " + user.getLogin());
            return;
        }

        JsonNode projectsArr = projectsJson.at(PATH_USER_PROJECTS);
        for (JsonNode projectJson : projectsArr) {
            ProjectState projectState = ProjectState.valueOf(projectJson.get(GOAL_STATUS).asText());
            if (!ProjectState.UNAVAILABLE.equals(projectState)) {
                userProjectService.createOrUpdate(user, projectJson);
            }
        }
    }
}
