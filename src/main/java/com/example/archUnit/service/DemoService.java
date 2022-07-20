package com.example.archUnit.service;


import com.example.archUnit.controller.DemoController;
import com.example.archUnit.dao.DemoDao;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

//    @Autowired
    DemoController demoController;

    void demoMethod1() {
        new DemoDao();
        new DemoService2();
        DemoController demoController = new DemoController();
    }
}
