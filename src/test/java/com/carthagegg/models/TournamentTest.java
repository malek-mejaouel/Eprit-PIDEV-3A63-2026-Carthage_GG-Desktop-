package com.carthagegg.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TournamentTest {

    private Tournament tournament;

    @BeforeAll
    void setup() {
        tournament = new Tournament();
    }

    @Test
    @Order(1)
    void testAjouterTournament() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 3);
        BigDecimal prizePool = new BigDecimal("5000.00");

        tournament.setTournamentId(7);
        tournament.setTournamentName("Spring Cup");
        tournament.setStartDate(startDate);
        tournament.setEndDate(endDate);
        tournament.setPrizePool(prizePool);
        tournament.setLocation("Tunis");
        tournament.setGameId(2);
        tournament.setUserId(9);

        assertEquals(7, tournament.getTournamentId());
        assertEquals("Spring Cup", tournament.getTournamentName());
        assertEquals(startDate, tournament.getStartDate());
        assertEquals(endDate, tournament.getEndDate());
        assertEquals(prizePool, tournament.getPrizePool());
        assertEquals("Tunis", tournament.getLocation());
        assertEquals(2, tournament.getGameId());
        assertEquals(9, tournament.getUserId());
    }

    @Test
    @Order(2)
    void testModifierTournament() {
        LocalDate newStartDate = LocalDate.of(2026, 6, 10);
        LocalDate newEndDate = LocalDate.of(2026, 6, 12);
        BigDecimal newPrizePool = new BigDecimal("7500.00");

        tournament.setTournamentName("Summer Cup");
        tournament.setStartDate(newStartDate);
        tournament.setEndDate(newEndDate);
        tournament.setPrizePool(newPrizePool);
        tournament.setLocation("Sousse");
        tournament.setGameId(3);
        tournament.setUserId(11);

        assertEquals("Summer Cup", tournament.getTournamentName());
        assertEquals(newStartDate, tournament.getStartDate());
        assertEquals(newEndDate, tournament.getEndDate());
        assertEquals(newPrizePool, tournament.getPrizePool());
        assertEquals("Sousse", tournament.getLocation());
        assertEquals(3, tournament.getGameId());
        assertEquals(11, tournament.getUserId());
    }

    @Test
    @Order(3)
    void testSupprimerTournament() {
        tournament.setTournamentId(0);
        tournament.setTournamentName(null);
        tournament.setStartDate(null);
        tournament.setEndDate(null);
        tournament.setPrizePool(null);
        tournament.setLocation(null);
        tournament.setGameId(0);
        tournament.setUserId(0);

        assertEquals(0, tournament.getTournamentId());
        assertNull(tournament.getTournamentName());
        assertNull(tournament.getStartDate());
        assertNull(tournament.getEndDate());
        assertNull(tournament.getPrizePool());
        assertNull(tournament.getLocation());
        assertEquals(0, tournament.getGameId());
        assertEquals(0, tournament.getUserId());
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("testSupprimerTournament")) {
            tournament = new Tournament();
        }
    }
}
