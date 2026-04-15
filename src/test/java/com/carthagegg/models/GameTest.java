package com.carthagegg.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameTest {

    private Game game;

    @BeforeAll
    void setup() {
        game = new Game();
    }

    @Test
    @Order(1)
    void testAjouterGame() {
        game.setGameId(1);
        game.setName("Valorant");
        game.setGenre("FPS");
        game.setDescription("Tactical shooter");

        assertEquals(1, game.getGameId());
        assertEquals("Valorant", game.getName());
        assertEquals("FPS", game.getGenre());
        assertEquals("Tactical shooter", game.getDescription());
        assertEquals("Valorant", game.toString());
    }

    @Test
    @Order(2)
    void testModifierGame() {
        game.setName("League of Legends");
        game.setGenre("MOBA");
        game.setDescription("Updated description");

        assertEquals("League of Legends", game.getName());
        assertEquals("MOBA", game.getGenre());
        assertEquals("Updated description", game.getDescription());
        assertEquals("League of Legends", game.toString());
    }

    @Test
    @Order(3)
    void testSupprimerGame() {
        game.setGameId(0);
        game.setName(null);
        game.setGenre(null);
        game.setDescription(null);

        assertEquals(0, game.getGameId());
        assertNull(game.getName());
        assertNull(game.getGenre());
        assertNull(game.getDescription());
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("testSupprimerGame")) {
            game = new Game();
        }
    }
}
