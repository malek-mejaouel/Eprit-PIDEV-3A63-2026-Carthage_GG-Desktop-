package com.carthagegg.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamTest {

    private Team team;

    @BeforeAll
    void setup() {
        team = new Team();
    }

    @Test
    @Order(1)
    void testAjouterTeam() {
        LocalDate creationDate = LocalDate.of(2026, 4, 15);

        team.setTeamId(10);
        team.setTeamName("Carthage Wolves");
        team.setLogo("logo.png");
        team.setCreationDate(creationDate);
        team.setUserId(5);

        assertEquals(10, team.getTeamId());
        assertEquals("Carthage Wolves", team.getTeamName());
        assertEquals("logo.png", team.getLogo());
        assertEquals(creationDate, team.getCreationDate());
        assertEquals(5, team.getUserId());
        assertEquals("Carthage Wolves", team.toString());
    }

    @Test
    @Order(2)
    void testModifierTeam() {
        LocalDate updatedDate = LocalDate.of(2026, 5, 1);

        team.setTeamName("Carthage Eagles");
        team.setLogo("new_logo.png");
        team.setCreationDate(updatedDate);
        team.setUserId(8);

        assertEquals("Carthage Eagles", team.getTeamName());
        assertEquals("new_logo.png", team.getLogo());
        assertEquals(updatedDate, team.getCreationDate());
        assertEquals(8, team.getUserId());
        assertEquals("Carthage Eagles", team.toString());
    }

    @Test
    @Order(3)
    void testSupprimerTeam() {
        team.setTeamId(0);
        team.setTeamName(null);
        team.setLogo(null);
        team.setCreationDate(null);
        team.setUserId(0);

        assertEquals(0, team.getTeamId());
        assertNull(team.getTeamName());
        assertNull(team.getLogo());
        assertNull(team.getCreationDate());
        assertEquals(0, team.getUserId());
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("testSupprimerTeam")) {
            team = new Team();
        }
    }
}
