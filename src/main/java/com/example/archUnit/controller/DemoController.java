package com.example.archUnit.controller;

import com.example.archUnit.dao.DemoDao;
import com.example.archUnit.domain.DemoDto;
import com.example.archUnit.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DemoController {

    @Resource
    DemoService service;

    public int ListNameByIds(@RequestBody List<Integer> ids) {
        return 1;
    }

    public int ListNameByIds(@RequestBody DemoDto demoDto) {
        return 1;
    }
}
