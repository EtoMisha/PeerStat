package edu.platform.service;

import edu.platform.models.Campus;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LoginService {
    private static final String URL = "https://edu.21-school.ru/";
    private static final String LOGIN_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[1]/div/input";
    private static final String PASSWORD_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[2]/div/input";
    private static final String BUTTON_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[3]/button";
    private static final String AUTHORITY = "edu.21-school.ru";
    private static final String GRAPHQL_URL = "https://edu.21-school.ru/services/graphql";

    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

    @Value("${parser.chrome-driver-path}")
    private String chromeDriverPath;

    public String getCookies(String login, String password) {
        LOG.info("Start login " + login);

        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();

        String cookiesStr;
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
            cookiesStr = cookiesSB.toString();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        LOG.info("Get cookies done " + cookiesStr);
        return cookiesStr;
    }

    public String sendRequest(Campus campus, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", campus.getCookie());
        headers.set("schoolId", campus.getSchoolId());
        headers.set("authority", AUTHORITY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String responseBody = "";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(GRAPHQL_URL, HttpMethod.GET, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            responseBody = response.getBody();
        } else {
            LOG.error("Response code " + response.getStatusCode());
        }

        return responseBody;
    }

}
