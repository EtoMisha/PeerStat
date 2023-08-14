package edu.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.platform.modelView.StatUserView;
import edu.platform.mapper.UserMapper;
import edu.platform.models.Campus;
import edu.platform.models.User;
import edu.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final String NO_BOOTCAMP = "No bootcamp";
    private static final String NO_COALITION = "No Coalition";
    private static final String NO_WAVE = "No wave";
    private static final String NO_EDU_FORM = "No edu form";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final TypeReference<Map<String, String>> TYPE_REFERENCE_STRING_MAP = new TypeReference<Map<String, String>> () {};

    public List<User> findUsersBySchoolId(String schoolId) {
        return userRepository.findUsersByCampusSchoolId(schoolId);
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
//    public void save(User user) {
//        userRepository.save(user);
//    }
//
    public void setCredentials(User user, JsonNode credentialsJson) {
        if (!credentialsJson.isEmpty()) {
            JsonNode studentJson = credentialsJson.get(SCHOOL_21).get(GET_STUDENT_BY_LOGIN);
            Map<String, String> credentialsMap = MAPPER.convertValue(studentJson, TYPE_REFERENCE_STRING_MAP);

            user.setStudentId(credentialsMap.get(STUDENT_ID));
            user.setUserId(credentialsMap.get(USER_ID));
            user.setActive(Boolean.parseBoolean(credentialsMap.get(IS_ACTIVE)));
            user.setGraduate(Boolean.parseBoolean(credentialsMap.get(IS_GRADUATE)));
        }
    }
//
//    public void setPersonalInfo(User user, JsonNode personalInfo) throws IOException {
//        if (!personalInfo.isEmpty()) {
//            JsonNode student = personalInfo.get(STUDENT);
//            JsonNode stageInfo = student.get(STAGE_INFO);
//            JsonNode xpInfo = student.get(XP_INFO);
//            JsonNode level = xpInfo.get(LEVEL);
//
//            int waveId = stageInfo.get(WAVE_ID) != null ? stageInfo.get(WAVE_ID).asInt() : 0;
//            String waveName = stageInfo.get(WAVE_NAME) != null ? stageInfo.get(WAVE_NAME).asText() : NO_WAVE;
//            String eduForm = stageInfo.get(EDU_FORM) != null ? stageInfo.get(EDU_FORM).asText() : NO_EDU_FORM;
//
//            user.setWaveId(waveId);
//            user.setWaveName(waveName);
//            user.setEduForm(eduForm);
//            user.setXp(xpInfo.get(VALUE).asInt());
//            user.setLevel(level.get(LEVEL_CODE).asInt());
//            user.setLeftBorder(level.get(RANGE).get(LEFT_BORDER).asInt());
//            user.setRightBorder(level.get(RANGE).get(RIGHT_BORDER).asInt());
//            user.setPeerPoints(xpInfo.get(PEER_POINTS).asInt());
//            user.setCoins(xpInfo.get(COINS_COUNT).asInt());
//            user.setCodeReviewPoints(xpInfo.get(CODE_REVIEW_POINTS).asInt());
//            user.setEmail(student.get(EMAIL).asText());
//        }
//    }
//
//    public void setCoalitionInfo(User user, JsonNode coalitionInfo) {
//        if (!coalitionInfo.isEmpty()) {
//            String coalitionName = null;
//            if (coalitionInfo.get(STUDENT) != null
//                    && coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME) != null) {
//                coalitionName = coalitionInfo.get(STUDENT).get(TOURNAMENT).get(MEMBER).get(COALITION).get(NAME).asText();
//            }
//            user.setCoalitionName((coalitionName != null && !coalitionName.isEmpty()) ? coalitionName : NO_COALITION);
//        }
//    }
//
//    public void setStageInfo(User user, JsonNode stageInfo) {
//        if (!stageInfo.isEmpty()) {
//            JsonNode school21 = stageInfo.get(SCHOOL_21);
//            JsonNode stageGroupsArr = school21.get(LOAD_STAGE_GROUPS);
//
//            String bootcampId = null;
//            String bootcampName = null;
//            for (JsonNode stageGroup : stageGroupsArr) {
//                String eduForm = stageGroup.get(STAGE_GROUPS).get(EDU_FORM).asText();
//                if (SURVIVAL_CAMP.equals(eduForm)) {
//                    bootcampId = stageGroup.get(STAGE_GROUPS).get(WAVE_ID).asText();
//                    bootcampName = stageGroup.get(STAGE_GROUPS).get(WAVE_NAME).asText();
//                }
//            }
//
//            if (bootcampId == null) {
//                bootcampId = NO_BOOTCAMP;
//                bootcampName = NO_BOOTCAMP;
//            }
//
//            user.setBootcampId(bootcampId);
//            user.setBootcampName(bootcampName);
//        }
//    }
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
