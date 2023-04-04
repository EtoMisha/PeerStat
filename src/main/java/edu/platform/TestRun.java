package edu.platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TestRun {

    private ApplicationContext context;

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws IOException {
//        Parser parser = context.getBean(Parser.class);
//        System.out.println("[PARSER] file " + parser.getUsersList());
//        System.out.println("[PARSER] schoolId " + parser.getSchoolId());
//        System.out.println("[PARSER] cookie " + parser.getCookie());
//
//        parser.initUsers();
    }

    @Autowired
    public void setContext(ApplicationContext context) { this.context = context; }
}
