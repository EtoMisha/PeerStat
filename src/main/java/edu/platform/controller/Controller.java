package edu.platform.controller;

import edu.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public ModelAndView index () {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("users", userService.getAllUsers());
        return modelAndView;
    }

    @RequestMapping("/test")
    public String test () {
        return "controller test ok";
    }

//
//    @GetMapping("/getUserInfo/")
//    public String getAllUsersInfo() {
//        return userService.getAllUsersInfo();
//    }
//
//    @GetMapping("/getUserInfo/{login}")
//    public String getUserInfo(@PathVariable String login) {
//        return userService.getUserInfo(login);
//    }
//
//    @GetMapping("/test")
//    public ModelAndView getTestInfo() {
//        ModelAndView modelAndView = new ModelAndView("index");
//        modelAndView.addObject("users", userService.getTestInfo());
//        System.out.println("getTestInfo " + userService.getTestInfo());
//        return modelAndView;
//    }

}