package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.models.Campus;
import edu.platform.models.User;
import edu.platform.parser.RequestBody;
import edu.platform.repository.CampusRepository;
import edu.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CampusService {
    private static final String PROPERTY_CAMPUS_LIST = "campus.list";
    private static final String PROPERTY_PREFIX = "campus.";
    private static final String PROPERTY_SCHOOL_ID = ".school-id";
    private static final String PROPERTY_NAME = ".name";
    private static final String PROPERTY_WAVE_PREFIX = ".wave-prefix";
    private static final String PROPERTY_LOGIN = ".login";
    private static final String PROPERTY_PASSWORD = ".password";
    private static final String STUDENT_POSTFIX = "@student";

    private final CampusRepository campusRepository;
    private final UserRepository userRepository;
    private final LoginService loginService;

    public List<Campus> getAll() {
        return campusRepository.findAll();
    }

    public void save(Campus campus) {
        campusRepository.save(campus);
    }

    public Campus getCampusById(String schoolId) {
        return campusRepository.findBySchoolId(schoolId);
    }






    public List<Campus> initCampusesFromProps(String propertiesName) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesName));
        List<String> campusTagsList = List.of(props.getProperty(PROPERTY_CAMPUS_LIST).split(","));
        System.out.println("[initCampus] campusTagsList " + campusTagsList);
        List<Campus> campusList = new ArrayList<>();
        for (String campusTag : campusTagsList) {
            Campus campus = createFromProperties(props, campusTag);
            setCookie(campus);
            campusOnlineUsers.put(campus, new ArrayList<>());
            save(campus);
            campusList.add(campus);
        }
        return campusList;
    }

    public Campus createFromProperties(Properties props, String campusTag) {
        System.out.println("[createFromProperties] " + campusTag);
        Campus campus = new Campus();
        campus.setName(campusTag);
        campus.setSchoolId(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_SCHOOL_ID));
        campus.setCampusName(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_NAME));
        campus.setWavePrefix(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_WAVE_PREFIX));
        campus.setUserFullLogin(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_LOGIN));
        campus.setUserLogin(campus.getUserFullLogin().substring(0, campus.getUserFullLogin().indexOf(STUDENT_POSTFIX)));
        campus.setUserPassword(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_PASSWORD));

        System.out.println("[createFromProperties] campus ok " + campus);

        return campus;
    }

    public void saveCookies(Campus campus) {
        setCookie(campus);
        save(campus);
    }

    private void setCookie(Campus campus) {
        campus.setCookie(loginService.getCookies(campus.getUserFullLogin(), campus.getUserPassword()));
    }

    public void updateUserLocations(Campus campus) {
        try {
            Map<Integer, String> clustersMap = new HashMap<>();
            Map<String, String> currentLocationsMap = new HashMap<>();

            JsonNode buildingInfo = loginService.sendRequest(campus, RequestBody.getBuildingInfo());
            if (buildingInfo != null) {
                JsonNode buildingsList = buildingInfo.get("student").get("getBuildings");
                for (JsonNode building : buildingsList) {
                    JsonNode clustersList = building.get("classrooms");
                    for (JsonNode cluster : clustersList) {
                        Integer clusterId = cluster.get("id").asInt();
                        String clusterName = cluster.get("number").asText();
                        clustersMap.put(clusterId, clusterName);
                    }
                }
            } else {
                System.out.println("[updateUserLocations] buildingInfo NULL");
            }

            for (Integer clusterId : clustersMap.keySet()) {
                JsonNode clusterPlanInfo = loginService.sendRequest(campus, RequestBody.getClusterPlanInfo(clusterId));
                if (clusterPlanInfo != null) {
                    JsonNode placesList = clusterPlanInfo.get("student").get("getClusterPlanStudentsByClusterId").get("occupiedPlaces");
                    for (JsonNode place : placesList) {
                        String location = clustersMap.get(clusterId) + " "
                                + place.get("row").asText() + "-"
                                + place.get("number").asInt();
                        String userLogin = place.get("user").get("login").asText();
                        currentLocationsMap.put(userLogin.substring(0, userLogin.indexOf("@")), location);
                    }
                }
            }

            for (String login : campusOnlineUsers.get(campus)) {
                if (!currentLocationsMap.containsKey(login)) {
                    currentLocationsMap.put(login, "");
                    campusOnlineUsers.get(campus).remove(login);
                }
            }

            userService.updateUsersLocation(currentLocationsMap);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
