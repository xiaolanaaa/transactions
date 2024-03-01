package com.huohu.controller;

import com.huohu.pojo.Position;
import com.huohu.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("position")
public class PositionController {
    @Autowired
    PositionService positionService;
    @RequestMapping("add")
    public void add(){
        Position position = new Position();
        position.setTranname("tme");
        positionService.save(position);
    }
}
