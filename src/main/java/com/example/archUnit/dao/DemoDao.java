package com.example.archUnit.dao;

import com.example.archUnit.service.DemoService;
import org.springframework.stereotype.Repository;

@Repository
public class DemoDao {

    public void demoMethod(){
        new DemoService();
    }
}
