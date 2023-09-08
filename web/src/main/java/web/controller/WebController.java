package web.controller;

import lombok.RequiredArgsConstructor;
import models.Project;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import service.ProjectService;
import service.UserProjectService;
import service.UserService;
import web.modelView.ProjectView;
import web.service.TelegramService;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class WebController {

    private final UserService userService;
    private final UserProjectService userProjectService;
    private final ProjectService projectService;
    private final TelegramService telegramService;
    private final ModelMapper modelMapper = new ModelMapper();

    @GetMapping("/test")
    public String getTest() {
        return "Test ok";
    }

    @GetMapping("/stat")
    public ModelAndView getStatPage (@RequestParam(defaultValue = "") String campus) {
        ModelAndView modelAndView = new ModelAndView("stat");
        if (campus != null && !campus.isEmpty()) {
            modelAndView.addObject("users", userService.findByCampusName(campus));
        }
        return modelAndView;
    }

    @GetMapping("/availability")
    public ModelAndView getAvailabilityPage() {
        return new ModelAndView("availability");
    }

    @GetMapping("/project")
    public ModelAndView getProjectsInfo(@RequestParam(defaultValue = "0") long projectId) {
        ModelAndView modelAndView = new ModelAndView("project");
        if (projectId != 0) {
            modelAndView.addObject("users", userProjectService.getProjectUsers(projectId));
            modelAndView.addObject("project", convertProject(projectService.findById(projectId)));
            // TODO 404 if !optional.isPresent
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

    private ProjectView convertProject(Project project) {
        return modelMapper.map(project, ProjectView.class);
    }
}