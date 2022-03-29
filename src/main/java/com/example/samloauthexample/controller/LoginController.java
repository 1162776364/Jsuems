package com.example.samloauthexample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping("/")
    public String login(){
        return "login";
    }

    @GetMapping("/success")
    public String successHtml(){
        return "success";
    }
}
