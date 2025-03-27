package com.monumentaltakehome.wallbuilder.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.monumentaltakehome.wallbuilder.response.WallDto;

public class FlemishBondServiceTest {

    private FlemishBondService wallService = new FlemishBondService();

    @Test
    public void testGenerateWall() {
       WallDto wall = wallService.generateWall();

        assertThat(wall.bricks()).isNotEmpty();
        assertThat(wall.bricks().size()).isEqualTo(448);
    }
}
