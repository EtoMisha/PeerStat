package edu.platform.service;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class LoginService {
    private static final String URL = "https://edu.21-school.ru/";
    private static final String LOGIN_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[1]/div/input";
    private static final String PASSWORD_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[2]/div/input";
    private static final String BUTTON_XPATH = "/html/body/div/div/div/div[2]/div/div/form/div[3]/button";

    @Value("${parser.login}")
    private String login;

    @Value("${parser.password}")
    private String password;

    @Value("${parser.chromeDriverPath}")
    private String chromeDriverPath;

    public String getCookies() {
        System.out.println("[getCookies] start login " + login + " pass " + password);

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
            TimeUnit.SECONDS.sleep(5);

            driver.findElement(By.xpath(LOGIN_XPATH)).sendKeys(login);
            driver.findElement(By.xpath(PASSWORD_XPATH)).sendKeys(password);
            driver.findElements(By.xpath(BUTTON_XPATH)).get(0).click();
            TimeUnit.SECONDS.sleep(5);

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

}
