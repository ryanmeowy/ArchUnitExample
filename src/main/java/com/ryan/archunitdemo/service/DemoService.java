package com.ryan.archunitdemo.service;


import com.ryan.archunitdemo.dao.DemoDao;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

//    @Autowired
//    DemoController demoController;

    void demoMethod1() {
        new DemoDao();
        new DemoService2();
    }
}
