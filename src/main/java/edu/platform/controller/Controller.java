package edu.platform.controller;

import edu.platform.service.TelegramService;
import edu.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {

    private UserService userService;
    private TelegramService telegramService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTelegramService(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @RequestMapping("/")
    public ModelAndView index () {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("users", userService.getAllUsers());
        return modelAndView;
    }

    @PostMapping("/mapForm")
    public String acceptMapForm(@RequestBody String request) {
        telegramService.sendToAdmin(request);
        return "Ок, записал, спасибо, попозже внесу на карту";
    }

    @RequestMapping("/test")
    public String test () {
        telegramService.sendToAdmin("test");
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