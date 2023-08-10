package edu.platform.service;

import edu.platform.models.Campus;
import edu.platform.parser.Parser;
import edu.platform.repository.CampusRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private final CampusRepository campusRepository;

    public List<Campus> getAll() {
        return campusRepository.findAll();
    }

    public void save(Campus campus) {
        campusRepository.save(campus);
    }

    public List<Campus> initCampusesFromProps(String propertiesName) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesName));
        List<String> campusTagsList = List.of(props.getProperty(PROPERTY_CAMPUS_LIST).split(","));
        LOG.info("Campuses tags " + campusTagsList);

        List<Campus> campusList = new ArrayList<>();
        campusTagsList.forEach(tag -> campusList.add(createFromProperties(props, tag)));
        return campusList;
    }

    public Campus createFromProperties(Properties props, String campusTag) {
        Campus campus = new Campus();
        campus.setName(campusTag);
        campus.setId(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_SCHOOL_ID));
        campus.setCampusName(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_NAME));
        campus.setWavePrefix(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_WAVE_PREFIX));
        campus.setUserFullLogin(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_LOGIN));
        campus.setUserLogin(campus.getUserFullLogin().substring(0, campus.getUserFullLogin().indexOf(STUDENT_POSTFIX)));
        campus.setUserPassword(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_PASSWORD));
        campusRepository.save(campus);

        LOG.info("Campus created: " + campus.getName());
        return campus;
    }

    public void setCookies(Campus campus, String cookies) {
        campus.setCookie(cookies);
        save(campus);
    }

//    public void updateUserLocations(Campus campus) {
//        try {
//            Map<Integer, String> clustersMap = new HashMap<>();
//            Map<String, String> currentLocationsMap = new HashMap<>();
//
//            JsonNode buildingInfo = loginService.sendRequest(campus, Request.getBuildingInfo());
//            if (buildingInfo != null) {
//                JsonNode buildingsList = buildingInfo.get("student").get("getBuildings");
//                for (JsonNode building : buildingsList) {
//                    JsonNode clustersList = building.get("classrooms");
//                    for (JsonNode cluster : clustersList) {
//                        Integer clusterId = cluster.get("id").asInt();
//                        String clusterName = cluster.get("number").asText();
//                        clustersMap.put(clusterId, clusterName);
//                    }
//                }
//            } else {
//                System.out.println("[updateUserLocations] buildingInfo NULL");
//            }
//
//            for (Integer clusterId : clustersMap.keySet()) {
//                JsonNode clusterPlanInfo = loginService.sendRequest(campus, Request.getClusterPlanInfo(clusterId));
//                if (clusterPlanInfo != null) {
//                    JsonNode placesList = clusterPlanInfo.get("student").get("getClusterPlanStudentsByClusterId").get("occupiedPlaces");
//                    for (JsonNode place : placesList) {
//                        String location = clustersMap.get(clusterId) + " "
//                                + place.get("row").asText() + "-"
//                                + place.get("number").asInt();
//                        String userLogin = place.get("user").get("login").asText();
//                        currentLocationsMap.put(userLogin.substring(0, userLogin.indexOf("@")), location);
//                    }
//                }
//            }
//
//            for (String login : campusOnlineUsers.get(campus)) {
//                if (!currentLocationsMap.containsKey(login)) {
//                    currentLocationsMap.put(login, "");
//                    campusOnlineUsers.get(campus).remove(login);
//                }
//            }
//
//            userService.updateUsersLocation(currentLocationsMap);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
}
