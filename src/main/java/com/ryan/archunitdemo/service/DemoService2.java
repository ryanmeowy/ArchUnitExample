package com.ryan.archunitdemo.service;

import com.ryan.archunitdemo.DemoImpl;
import org.springframework.stereotype.Service;

@Service
public class DemoService2 {

    public void demoMethod() {
        System.err.println("demo method is running");
    }

    void demo() {
        new DemoImpl();
    }
}
