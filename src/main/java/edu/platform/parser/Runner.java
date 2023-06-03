package edu.platform.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Runner {
    private static final String TEST_MODE = "test";
    private static final String INIT_MODE = "init";
    private static final String UPDATE_MODE = "update";

    @Value("${parser.mode}")
    private String mode;
    @Value("${parser.runtime}")
    private String runTimeSetting;
    @Value("${parser.plusDays}")
    private String plusDays;

    private Parser parser;

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStart() {

        System.out.println("[PARSER] run mode " + mode);
        if (TEST_MODE.equals(mode)) {
            System.out.println("[PARSER] testInit");
            parser.testInit();

        } else if (INIT_MODE.equals(mode)) {
            System.out.println("[PARSER] initUsers");
            parser.initUsers();

        } else if (UPDATE_MODE.equals(mode)) {
            System.out.println("[PARSER] updateUsers");
            parser.updateUsers();
        }

        scheduleRun();
    }

    private void scheduleRun() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        LocalTime runTime = LocalTime.parse(runTimeSetting);
        long delay = LocalDateTime.now().until(LocalDate.now()
                .plusDays(Integer.parseInt(plusDays))
                .atTime(runTime), ChronoUnit.MINUTES);
        System.out.println("[PARSER] scheduleRun time " + runTime + " delay " + delay);

        final Runnable scheduleRunner = this::scheduleUpdate;
        scheduler.scheduleAtFixedRate(scheduleRunner, delay,
                TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    private void scheduleUpdate() {
        System.out.println("[PARSER] scheduleUpdate " + LocalDateTime.now());
        parser.updateUsers();
    }

    private void scheduleTest() {
        parser.testInit();
        parser.setLastUpdateTime();
        System.out.println("SHEDULE TEST OK " + LocalDateTime.now());
    }

    @Autowired
    public void setParser(Parser parser) {
        this.parser = parser;
    }
}
