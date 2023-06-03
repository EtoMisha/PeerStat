package edu.platform.parser;

import edu.platform.models.Campus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Runner {
    private static final String CUSTOM_PROPERTIES = "custom.properties";

    private static final String TEST_MODE = "test";
    private static final String INIT_MODE = "init";
    private static final String UPDATE_MODE = "update";

    @Value("${run.mode}")
    private String mode;
    @Value("${run.runtime}")
    private String runTimeSetting;
    @Value("${run.plus-days}")
    private String plusDays;

    private Parser parser;
    private final List<Campus> campusList = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStart() {
        System.out.println("[Runner] run mode " + mode);

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(CUSTOM_PROPERTIES));

            campusList.add(new Campus("msk", props));
            campusList.add(new Campus("kzn", props));
            campusList.add(new Campus("nsk", props));

        } catch (IOException e) {
            System.out.println("[Runner] ERROR " + e.getMessage());
        }

        if (TEST_MODE.equals(mode)) {
            campusList.forEach(parser::testInit);
        } else if (INIT_MODE.equals(mode)) {
            campusList.forEach(parser::initUsers);
        } else if (UPDATE_MODE.equals(mode)) {
            campusList.forEach(parser::updateUsers);
        }

        scheduleRun();
    }

    private void scheduleRun() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        LocalTime runTime = LocalTime.parse(runTimeSetting);
        long delay = LocalDateTime.now().until(LocalDate.now()
                .plusDays(Integer.parseInt(plusDays))
                .atTime(runTime), ChronoUnit.MINUTES);

        System.out.println("[Runner] scheduleRun time " + runTime + " delay " + delay);

        final Runnable scheduleRunner = this::scheduleUpdate;
        scheduler.scheduleAtFixedRate(scheduleRunner, delay,
                TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    private void scheduleUpdate() {
        System.out.println("[Runner] scheduleUpdate " + LocalDateTime.now());
        campusList.forEach(parser::updateUsers);
    }

    @Autowired
    public void setParser(Parser parser) {
        this.parser = parser;
    }
}
