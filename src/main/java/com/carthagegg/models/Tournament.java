package com.carthagegg.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Tournament {
    private int tournamentId;
    private String tournamentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal prizePool;
    private String location;
    private int gameId;
    private int userId;

    public Tournament() {}

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getPrizePool() { return prizePool; }
    public void setPrizePool(BigDecimal prizePool) { this.prizePool = prizePool; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
