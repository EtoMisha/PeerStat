package edu.platform.controller;


import edu.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/")
public class HtmlController {

    @RequestMapping("/")
    public ModelAndView index () {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.html");
        return modelAndView;
    }

}


//    @RequestMapping("/")
//    public ModelAndView index () {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("index");
//        return modelAndView;
//    }
