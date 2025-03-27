package com.monumentaltakehome.wallbuilder.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.monumentaltakehome.wallbuilder.response.WallDto;

public class StretcherBondServiceTest {

    private StretcherBondService wallService = new StretcherBondService();

    @Test
    public void testGenerateWall() {
        WallDto wall = wallService.generateWall();

        assertThat(wall.bricks()).isNotEmpty();
        assertThat(wall.bricks().size()).isEqualTo(352);
    }
}
