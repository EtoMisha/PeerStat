package service;

import models.Campus;
import repository.CampusRepository;
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

    private static final Logger LOG = LoggerFactory.getLogger(CampusService.class);

    private static final String PROPERTY_CAMPUS_LIST = "campus.list";
    private static final String PROPERTY_PREFIX = "campus.";
    private static final String PROPERTY_SCHOOL_ID = ".school-id";
    private static final String PROPERTY_NAME = ".name";
    private static final String PROPERTY_WAVE_PREFIX = ".wave-prefix";
    private static final String PROPERTY_LOGIN = ".login";
    private static final String PROPERTY_PASSWORD = ".password";
    private static final String STUDENT_POSTFIX = "@student";

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
}
