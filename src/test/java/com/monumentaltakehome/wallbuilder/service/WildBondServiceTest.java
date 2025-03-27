package com.monumentaltakehome.wallbuilder.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.monumentaltakehome.wallbuilder.response.WallDto;

//@SpringBootTest
public class WildBondServiceTest {

    private WildBondService wallService = new WildBondService();

    @Test
    public void testGenerateWall() {
        WallDto wall = wallService.generateWall();

        assertThat(wall.bricks()).isNotEmpty();
    }
}
