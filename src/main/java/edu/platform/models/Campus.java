package edu.platform.models;

import lombok.Data;

import java.util.Properties;

@Data
public class Campus {
    private static final String PROPERTY_PREFIX = "parser.";
    private static final String PROPERTY_LOGIN = ".login";
    private static final String PROPERTY_PASSWORD = ".password";
    private static final String PROPERTY_SCHOOL_ID = ".school-id";

    private String login;
    private String password;
    private String schoolId;

    public Campus(String campusName, Properties props) {
        this.login = props.getProperty(PROPERTY_PREFIX + campusName + PROPERTY_LOGIN);
        this.password = props.getProperty(PROPERTY_PREFIX + campusName + PROPERTY_PASSWORD);
        this.schoolId = props.getProperty(PROPERTY_PREFIX + campusName + PROPERTY_SCHOOL_ID);
    }
}
