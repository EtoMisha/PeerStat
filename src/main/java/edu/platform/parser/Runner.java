package edu.platform.parser;

import edu.platform.models.Campus;
import edu.platform.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RequiredArgsConstructor
@Component
public class Runner {
    private static final String CAMPUS_PROPERTIES = "campus.properties";
    private static final String TEST_MODE = "test";
    private static final String INIT_MODE = "init";
    private static final String UPDATE_MODE = "update";

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    @Value("${run.mode}")
    private String mode;
    @Value("${run.update-graph}")
    private boolean updateGraph;

    private final Parser parser;
    private final CampusService campusService;

    private List<Campus> campusList;

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStart() {
        LOG.info("Run mode " + mode);

        try {
            campusList = campusService.initCampusesFromProps(CAMPUS_PROPERTIES);
            parser.parseCampusInfo(campusList);

            if (TEST_MODE.equals(mode)) {
                campusList.forEach(parser::testInit);
            } else if (INIT_MODE.equals(mode)) {
                campusList.forEach(parser::initUsers);
            } else if (UPDATE_MODE.equals(mode)) {
                campusList.forEach(parser::updateUsers);
            }

            scheduleUpdate(this::dataUpdate, TimeUnit.DAYS.toMinutes(1));
            scheduleUpdate(this::cookieUpdate, TimeUnit.HOURS.toMinutes(6));
            scheduleUpdate(this::workplacesUpdate, TimeUnit.MINUTES.toMinutes(10));

        } catch (IOException e) {
            LOG.error("Runner error " + e.getMessage());
        }
    }

    private void scheduleUpdate(Runnable operation, long delay) {
        LOG.info("Schedule " + operation + " delay " + delay);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(operation, delay, delay, TimeUnit.MINUTES);
    }

    private void dataUpdate() {
        LOG.info("Data Update " + LocalDateTime.now());
        campusList.forEach(parser::updateUsers);
    }

    private void cookieUpdate() {
        LOG.info("Cookie Update " + LocalDateTime.now());
        campusList.forEach(parser::updateCookies);
    }

    private void workplacesUpdate() {
        LOG.info("Workplaces Update " + LocalDateTime.now());
        campusList.forEach(parser::parseWorkplaces);
    }
}
