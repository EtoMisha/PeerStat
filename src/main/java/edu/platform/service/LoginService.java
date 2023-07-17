package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.models.Campus;
import edu.platform.models.User;
import edu.platform.parser.RequestBody;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static edu.platform.constants.GraphQLConstants.DATA;

@Service
public class LoginService {
    private static final String URL = "https://edu.21-school.ru/";
    private static final String LOGIN_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[1]/div/input";
    private static final String PASSWORD_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[2]/div/input";
    private static final String BUTTON_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[3]/button";

    private final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUTHORITY = "edu.21-school.ru";
    private static final String GRAPHQL_URL = "https://edu.21-school.ru/services/graphql";

    @Value("${parser.chrome-driver-path}")
    private String chromeDriverPath;

    public String getCookies(String login, String password) {
        System.out.println("[getCookies] start login " + login);

        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();

        try {
            driver.get(URL);
            TimeUnit.SECONDS.sleep(1);

            driver.findElement(By.xpath(LOGIN_XPATH)).sendKeys(login);
            driver.findElement(By.xpath(PASSWORD_XPATH)).sendKeys(password);
            driver.findElements(By.xpath(BUTTON_XPATH)).get(0).click();
            TimeUnit.SECONDS.sleep(1);

            Set<Cookie> cookies = driver.manage().getCookies();

            StringBuilder cookiesSB = new StringBuilder();
            for (Cookie cookie : cookies) {
                cookiesSB.append(cookie.getName())
                        .append("=")
                        .append(cookie.getValue())
                        .append(";");
            }
            return cookiesSB.toString();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }

    public String getUserLocation(User user) {
        String location = "";
        try {
            JsonNode personalInfo = sendRequest(user.getCampus(), RequestBody.getPersonalInfo(user));
            JsonNode workStation = personalInfo.get("student").get("getWorkstationByLogin");
            if (workStation != null) {
                location = workStation.get("hostName").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return location;
    }

    public JsonNode sendRequest(Campus campus, String requestBody) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", campus.getCookie());
        headers.set("schoolId", campus.getSchoolId());
        headers.set("authority", AUTHORITY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        String responseStr = "";
        RestTemplate restTemplate = new RestTemplate();
        try {
            responseStr = restTemplate.postForObject(GRAPHQL_URL, request, String.class);
        } catch (RestClientException e) {
            System.out.println("[PARSER] ERROR " + e.getMessage());
        }
        return MAPPER.readTree(responseStr).get(DATA);
    }

}
