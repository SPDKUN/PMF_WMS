package com.neuedu.pmf.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping("/h")
    public String hello() {
        return "hello";
    }

}
