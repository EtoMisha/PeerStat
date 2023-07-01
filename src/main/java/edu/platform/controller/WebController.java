package edu.platform.controller;

import edu.platform.modelView.ProjectView;
import edu.platform.service.ProjectService;
import edu.platform.service.TelegramService;
import edu.platform.service.UserProjectService;
import edu.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class WebController {

    private UserService userService;
    private UserProjectService userProjectService;
    private ProjectService projectService;
    private TelegramService telegramService;

    @GetMapping("/stat")
    public ModelAndView getStatPage () {
        ModelAndView modelAndView = new ModelAndView("stat");
        modelAndView.addObject("users", userService.getAllUsers());
        return modelAndView;
    }

    @GetMapping("/availability")
    public ModelAndView getProjectsPage() {
        return new ModelAndView("availability");
    }

    @GetMapping("/project")
    public ModelAndView getProjectsInfo(@RequestParam(defaultValue = "0") long id) {
        ModelAndView modelAndView = new ModelAndView("project");
        if (id != 0) {
            modelAndView.addObject("users", userProjectService.getProjectUsersList(id));
            modelAndView.addObject("project", projectService.getProjectInfo(id));
        }
        return modelAndView;
    }

    @GetMapping("/projectList")
    public List<ProjectView> getProjectList() {
        return projectService.getProjectListForWeb();
    }

    @PostMapping("/mapForm")
    public String acceptMapForm(@RequestBody String request) {
        return telegramService.sendFormDataToAdmin(request);
    }


    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setUserProjectService(UserProjectService userProjectService) {
        this.userProjectService = userProjectService;
    }

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setTelegramService(TelegramService telegramService) {
        this.telegramService = telegramService;
    }
}