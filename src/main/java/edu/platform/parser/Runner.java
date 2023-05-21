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
    private static final String TEST_MODE = "test";
    private static final String INIT_MODE = "init";
    private static final String UPDATE_MODE = "update";

    @Value("${parser.mode}")
    private String mode;
    @Value("${parser.runtime}")
    private String runTimeSetting;

    private Parser parser;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        parser.login();
//        parser.testInit();
//        parser.initUsers();

        LocalTime runTime = LocalTime.parse(runTimeSetting);
        System.out.println("[run] runTime " + runTime);

        long delay = ChronoUnit.MILLIS.between(LocalTime.now(), runTime);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(parser::updateUsers, delay, TimeUnit.MILLISECONDS);
//
//        System.out.println("[PARSER] run mode " + mode);
//        parser.login();
//        if (TEST_MODE.equals(mode)) {
//            System.out.println("[PARSER] testInit");
//            parser.testInit();
//
//        } else if (INIT_MODE.equals(mode)) {
//            System.out.println("[PARSER] initUsers");
//            parser.initUsers();
//
//        } else if (UPDATE_MODE.equals(mode)) {
//            System.out.println("[PARSER] updateUsers");
//            parser.updateUsers();
//        } else {
//            System.out.println("[PARSER] testInit elseeee");
//            parser.testInit();
//        }
    }

    @Autowired
    public void setParser(Parser parser) {
        this.parser = parser;
    }
}
