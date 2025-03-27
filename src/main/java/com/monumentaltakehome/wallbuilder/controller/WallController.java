package com.monumentaltakehome.wallbuilder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monumentaltakehome.wallbuilder.response.WallDto;
import com.monumentaltakehome.wallbuilder.service.FlemishBondService;
import com.monumentaltakehome.wallbuilder.service.StretcherBondService;
import com.monumentaltakehome.wallbuilder.service.WildBondService;


@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500") // Allow requests from this origin
public class WallController {

    @Autowired
    private StretcherBondService stretcherBondService;

    @Autowired
    private FlemishBondService flemishBondService;

    @Autowired
    private WildBondService wildBondService;

    @GetMapping("/stretcher-bond")
    public WallDto getHalfsteensverband() {
        return stretcherBondService.generateWall();
    }

    @GetMapping("/flemish-bond")
    public WallDto getFlemish() {
        return flemishBondService.generateWall();
    }

    @GetMapping("/wild-bond")
    public WallDto getWildverband() {
        return wildBondService.generateWall();
    }
}
