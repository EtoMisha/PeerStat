package edu.platform.controller;

import edu.platform.service.ProjectService;
import edu.platform.service.TelegramService;
import edu.platform.service.UserProjectService;
import edu.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class WebController {

    private final UserService userService;
    private final UserProjectService userProjectService;
    private final ProjectService projectService;
    private final TelegramService telegramService;

    @GetMapping("/test")
    public String getTest() {
        return "Test ok";
    }
//
//    @GetMapping("/stat")
//    public ModelAndView getStatPage (@RequestParam(defaultValue = "") String campus) {
//        ModelAndView modelAndView = new ModelAndView("stat");
//        if (campus != null && !campus.isEmpty()) {
////            modelAndView.addObject("users", userService.getAllUsers());
//            modelAndView.addObject("users", userService.findUsersByCampusName(campus));
//        }
//        return modelAndView;
//    }
//
//    @GetMapping("/availability")
//    public ModelAndView getProjectsPage() {
//        return new ModelAndView("availability");
//    }
//
//    @GetMapping("/project")
//    public ModelAndView getProjectsInfo(@RequestParam(defaultValue = "0") long id) {
//        ModelAndView modelAndView = new ModelAndView("project");
//        if (id != 0) {
//            modelAndView.addObject("users", userProjectService.getProjectUsersList(id));
//            modelAndView.addObject("project", projectService.getProjectInfo(id));
//        }
//        return modelAndView;
//    }
//
//    @GetMapping("/projectList")
//    public List<ProjectView> getProjectList() {
//        return projectService.getProjectListForWeb();
//    }
//
//    @PostMapping("/mapForm")
//    public String acceptMapForm(@RequestBody String request) {
//        return telegramService.sendFormDataToAdmin(request);
//    }

}