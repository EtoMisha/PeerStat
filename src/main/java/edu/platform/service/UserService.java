package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.constants.ProjectState;
import edu.platform.constants.UserStatus;
import edu.platform.models.*;
import edu.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final String NO_BOOTCAMP = "No bootcamp";
    private static final String NO_COALITION = "No Coalition";
    private static final String NO_WAVE = "No wave";
    private static final String NO_EDU_FORM = "No edu form";

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

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

        JsonNode feedbackArrJson = student.at(PATH_FEEDBACK);
        feedbackService.createOrUpdate(user, feedbackArrJson);
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

    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> findUsersByCampus(Campus campus) {
        return userRepository.findUsersByCampus(campus);
    }












//    public List<User> getCampusUsers(Long campusId, Pageable pageable) {
//        return userRepository.findUsersByCampusId(campusId, pageable).getContent();
//    }
//
//
//
//    public List<StatUserView> getAllUsers() {
//        List<User> userList = userRepository.findByOrderByXpDesc();
//        return userList.stream()
//                .map(userMapper::getUserStatView)
//                .toList();
//    }
//
//    public User findUserByLogin(String login) {
//        return userRepository.findUserByLogin(login);
//    }
//
//
//
//    public List<StatUserView> findUsersByCampusName(String campusName) {
//        List<User> userList = userRepository.findUsersByCampusName(campusName);
//        return userList.stream()
//                .map(userMapper::getUserStatView)
//                .toList();
//    }
//

//

//
//
//
//
//    public void setXpHistory(User user, JsonNode xpHistoryInfo) {
//        if (!xpHistoryInfo.isEmpty()) {
//            JsonNode historyList = xpHistoryInfo.get(STUDENT).get(XP_HISTORY).get(HISTORY);
//
//            ArrayNode historyData = MAPPER.createArrayNode();
//            for (JsonNode history : historyList) {
//                ObjectNode objectHistory = history.deepCopy();
//                objectHistory.remove(TYPENAME);
//                historyData.add(objectHistory);
//            }
//
//            user.setXpHistory(historyData.toString());
//        }
//    }
//
//    public void updateUsersLocation(Map<String, String> usersLocationsMap) {
//        for (String login : usersLocationsMap.keySet()) {
//            User user = userRepository.findUserByLogin(login);
//            if (user != null) {
//                user.setLocation(usersLocationsMap.get(login));
//                save(user);
//            }
//        }
//        System.out.println("[updateUsersLocations] done " + LocalDateTime.now());
//    }

}
