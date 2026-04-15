package com.carthagegg.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MatchTest {

    private Match match;

    @BeforeAll
    void setup() {
        match = new Match();
    }

    @Test
    @Order(1)
    void testAjouterMatch() {
        LocalDateTime matchDate = LocalDateTime.of(2026, 6, 10, 18, 30);

        match.setMatchId(4);
        match.setMatchDate(matchDate);
        match.setScoreTeamA(2);
        match.setScoreTeamB(1);
        match.setTournamentId(11);
        match.setGameId(3);
        match.setTeamAId(20);
        match.setTeamBId(21);
        match.setTeamAName("Alpha");
        match.setTeamBName("Beta");
        match.setTournamentName("Final Stage");
        match.setStatus("Scheduled");

        assertEquals(4, match.getMatchId());
        assertEquals(matchDate, match.getMatchDate());
        assertEquals(2, match.getScoreTeamA());
        assertEquals(1, match.getScoreTeamB());
        assertEquals(11, match.getTournamentId());
        assertEquals(3, match.getGameId());
        assertEquals(20, match.getTeamAId());
        assertEquals(21, match.getTeamBId());
        assertEquals("Alpha", match.getTeamAName());
        assertEquals("Beta", match.getTeamBName());
        assertEquals("Final Stage", match.getTournamentName());
        assertEquals("Scheduled", match.getStatus());
    }

    @Test
    @Order(2)
    void testModifierMatch() {
        LocalDateTime updatedDate = LocalDateTime.of(2026, 6, 12, 20, 0);

        match.setMatchDate(updatedDate);
        match.setScoreTeamA(3);
        match.setScoreTeamB(2);
        match.setTournamentId(12);
        match.setGameId(5);
        match.setTeamAId(30);
        match.setTeamBId(31);
        match.setTeamAName("Gamma");
        match.setTeamBName("Delta");
        match.setTournamentName("Semi Final");
        match.setStatus("Finished");

        assertEquals(updatedDate, match.getMatchDate());
        assertEquals(3, match.getScoreTeamA());
        assertEquals(2, match.getScoreTeamB());
        assertEquals(12, match.getTournamentId());
        assertEquals(5, match.getGameId());
        assertEquals(30, match.getTeamAId());
        assertEquals(31, match.getTeamBId());
        assertEquals("Gamma", match.getTeamAName());
        assertEquals("Delta", match.getTeamBName());
        assertEquals("Semi Final", match.getTournamentName());
        assertEquals("Finished", match.getStatus());
    }

    @Test
    @Order(3)
    void testSupprimerMatch() {
        match.setMatchId(0);
        match.setMatchDate(null);
        match.setScoreTeamA(0);
        match.setScoreTeamB(0);
        match.setTournamentId(0);
        match.setGameId(0);
        match.setTeamAId(0);
        match.setTeamBId(0);
        match.setTeamAName(null);
        match.setTeamBName(null);
        match.setTournamentName(null);
        match.setStatus(null);

        assertEquals(0, match.getMatchId());
        assertNull(match.getMatchDate());
        assertEquals(0, match.getScoreTeamA());
        assertEquals(0, match.getScoreTeamB());
        assertEquals(0, match.getTournamentId());
        assertEquals(0, match.getGameId());
        assertEquals(0, match.getTeamAId());
        assertEquals(0, match.getTeamBId());
        assertNull(match.getTeamAName());
        assertNull(match.getTeamBName());
        assertNull(match.getTournamentName());
        assertNull(match.getStatus());
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("testSupprimerMatch")) {
            match = new Match();
        }
    }
}
