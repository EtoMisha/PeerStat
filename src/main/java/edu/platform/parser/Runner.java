package edu.platform.parser;

import edu.platform.models.Campus;
import edu.platform.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Runner {
    private static final String CAMPUS_PROPERTIES = "campus.properties";

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
    private CampusService campusService;
    private List<Campus> campusList;

    @Autowired
    public void setParser(Parser parser) {
        this.parser = parser;
    }

    @Autowired
    public void setCampusService(CampusService campusService) {
        this.campusService = campusService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStart() {
        System.out.println("[Runner] run mode " + mode);

        try {
            campusList = campusService.initCampusesFromProps(CAMPUS_PROPERTIES);
            System.out.println("[Runner] campusList " + campusList);

            parser.parseGraphInfo(campusList.get(0));

            if (TEST_MODE.equals(mode)) {
                campusList.forEach(parser::testInit);
            } else if (INIT_MODE.equals(mode)) {
                campusList.forEach(parser::initUsers);
            } else if (UPDATE_MODE.equals(mode)) {
                campusList.forEach(parser::updateUsers);
            }

            scheduleRun();

        } catch (IOException e) {
            System.out.println("[Runner] ERROR " + e.getMessage());
        }
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
}
