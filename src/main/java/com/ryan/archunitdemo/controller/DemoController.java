package com.ryan.archunitdemo.controller;

import com.ryan.archunitdemo.dao.DemoDao;
import org.springframework.stereotype.Controller;

@Controller
public class DemoController {

    void demo() {
        DemoDao demoDao = new DemoDao();
    }
}
