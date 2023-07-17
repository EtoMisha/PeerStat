package edu.platform.service;

import edu.platform.models.Campus;
import edu.platform.repository.CampusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    private CampusRepository campusRepository;
    private LoginService loginService;

    @Autowired
    public void setCampusRepository(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    @Autowired
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    public List<Campus> getAllCampuses() {
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
        campus.setFullLogin(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_LOGIN));
        campus.setLogin(campus.getFullLogin().substring(0, campus.getFullLogin().indexOf(STUDENT_POSTFIX)));
        campus.setPassword(props.getProperty(PROPERTY_PREFIX + campusTag + PROPERTY_PASSWORD));

        System.out.println("[createFromProperties] campus ok " + campus);

        return campus;
    }

    public void saveCookies(Campus campus) {
        setCookie(campus);
        save(campus);
    }

    private void setCookie(Campus campus) {
        campus.setCookie(loginService.getCookies(campus.getLogin(), campus.getPassword()));
    }
}
