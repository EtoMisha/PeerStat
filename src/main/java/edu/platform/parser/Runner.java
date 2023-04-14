package edu.platform.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Runner {

    private Parser parser;

    @Value("${parser.runtime}")
    private String runTimeSetting;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        testInit();
//        initUsers();

        LocalTime runTime = LocalTime.parse(runTimeSetting);
        System.out.println("[run] rinTime " + runTime);

        long delay = ChronoUnit.MILLIS.between(LocalTime.now(), runTime);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(this::updateUsers, delay, TimeUnit.MILLISECONDS);
    }

    private void updateUsers() {
        parser.updateUsers();
    }

    private void testInit() {
        parser.testInit();
    }

    private void initUsers() {
        System.out.println("[PARSER] file " + parser.getUsersList());
        System.out.println("[PARSER] schoolId " + parser.getSchoolId());
        System.out.println("[PARSER] cookie " + parser.getCookie());

        parser.initUsers();
    }

    @Autowired
    public void setContext(Parser parser) { this.parser = parser; }
}
