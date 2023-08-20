package edu.platform.parser;

import edu.platform.models.Campus;
import edu.platform.service.CampusService;
import edu.platform.service.ProjectService;
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
    @Value("${run.runtime}")
    private String runTimeSetting;
    @Value("${run.plus-days}")
    private String plusDays;
    @Value("${run.update-graph}")
    private boolean updateGraph;

    private final Parser parser;
    private final CampusService campusService;
    private final ProjectService projectService;

    private List<Campus> campusList;

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStart() {
        LOG.info("Run mode " + mode);

        try {
            campusList = campusService.initCampusesFromProps(CAMPUS_PROPERTIES);
            LOG.info("Campus list " + campusList);
            campusList.forEach(parser::updateCookies);

            if (updateGraph) {
                Campus campus = campusList.get(0);

                parser.parseGraphInfo(campus);
                projectService.getAll().stream()
                        .filter(project -> project.getProjectType() == null)
                        .forEach(project -> parser.updateProjects(campus, project));
            }

            if (TEST_MODE.equals(mode)) {
                campusList.forEach(parser::testInit);
            } else if (INIT_MODE.equals(mode)) {
                campusList.forEach(parser::initUsers);
            } else if (UPDATE_MODE.equals(mode)) {
                campusList.forEach(parser::updateUsers);
            }

            scheduleDataUpdate();
            scheduleCookieUpdate();
            scheduleLocationsUpdate();

        } catch (IOException e) {
            LOG.error("Runner error " + e.getMessage());
        }
    }

    private void scheduleDataUpdate() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        LocalTime runTime = LocalTime.parse(runTimeSetting);
        long delay = LocalDateTime.now().until(LocalDate.now()
                .plusDays(Integer.parseInt(plusDays))
                .atTime(runTime), ChronoUnit.MINUTES);

        LOG.info("ScheduleDataUpdate time " + runTime + " delay " + delay);

        final Runnable scheduleRunner = this::dataUpdate;
        scheduler.scheduleAtFixedRate(scheduleRunner, delay,
                TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    private void scheduleCookieUpdate() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        long period = TimeUnit.HOURS.toMinutes(6);
        LOG.info("ScheduleCookieUpdate period " + period);

        final Runnable scheduleRunner = this::cookieUpdate;
        scheduler.scheduleAtFixedRate(scheduleRunner, period, period, TimeUnit.MINUTES);
    }

    private void scheduleLocationsUpdate() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        long period = TimeUnit.MINUTES.toMinutes(10);
        LOG.info("ScheduleLocationsUpdate period " + period);

        final Runnable scheduleRunner = this::locationsUpdate;
        scheduler.scheduleAtFixedRate(scheduleRunner, TimeUnit.MINUTES.toMinutes(1), period, TimeUnit.MINUTES);
    }

    private void dataUpdate() {
        LOG.info("Data Update " + LocalDateTime.now());
        campusList.forEach(parser::updateUsers);
    }

    private void cookieUpdate() {
        LOG.info("CookieUpdate " + LocalDateTime.now());
        campusList.forEach(parser::updateCookies);
    }

    private void locationsUpdate() {
        campusList.forEach(parser::updateWorkplaces);
    }
}
