package web.controller;

import lombok.RequiredArgsConstructor;
import models.Project;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import service.ProjectService;
import service.UserProjectService;
import service.UserService;
import web.mapper.ProjectMapper;
import web.mapper.UserMapper;
import web.modelView.ProjectLinkView;
import web.modelView.UserProjectView;
import web.modelView.UserStatView;
import web.service.TelegramService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class WebController {

    private final UserService userService;
    private final UserProjectService userProjectService;
    private final ProjectService projectService;
    private final TelegramService telegramService;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;

    @GetMapping("/test")
    public String getTest() {
        return "Test ok";
    }

    @GetMapping("/stat")
    public ModelAndView getStatPage() {
        return new ModelAndView("stat");
    }

    @GetMapping("/stat/{campusName}")
    public ModelAndView getStatDataPage(@PathVariable String campusName) {
        ModelAndView modelAndView = new ModelAndView("stat");
        if (campusName != null && !campusName.isEmpty()) {
            List<UserStatView> userStatViewList = userService.findByCampusName(campusName).stream()
                    .map(userMapper::getUserStatView).toList();
            modelAndView.addObject("users", userStatViewList);
        }
        return modelAndView;
    }

    @GetMapping("/availability")
    public ModelAndView getAvailabilityPage() {
        return new ModelAndView("availability");
    }

    @GetMapping("/project")
    public ModelAndView getProjectsPage() {
        ModelAndView modelAndView = new ModelAndView("project");
        List<ProjectLinkView> projectLinksList = projectService.findAll().stream()
                .map(projectMapper::getProjectLinkView).toList();
        modelAndView.addObject("projectLinks", projectLinksList);
        return modelAndView;
    }

    @GetMapping("/project/{projectId}")
    public ModelAndView getProjectDataInfo(@PathVariable long projectId) {
        ModelAndView modelAndView = new ModelAndView("project");
        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isPresent()) {
            modelAndView.addObject("project", projectMapper.getProjectView(projectOpt.get()));

            List<UserProjectView> userProjectViewList = userProjectService.getProjectUsers(projectId).stream()
                    .map(userMapper::getUserProjectView).toList();
            modelAndView.addObject("users", userProjectViewList);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return modelAndView;
    }

    @PostMapping("/mapForm")
    public String acceptMapForm(@RequestBody String request) {
        return telegramService.sendFormDataToAdmin(request);
    }
}