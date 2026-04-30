package com.carthagegg.models;

import java.time.LocalDateTime;

public class Match {
    private int matchId;
    private LocalDateTime matchDate;
    private int scoreTeamA;
    private int scoreTeamB;
    private int tournamentId;
    private int gameId;
    private int teamAId;
    private int teamBId;
    private String teamAName;
    private String teamBName;
    private String tournamentName;
    private String status;
    private boolean isRivalry;

    public Match() {}

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public int getScoreTeamA() { return scoreTeamA; }
    public void setScoreTeamA(int scoreTeamA) { this.scoreTeamA = scoreTeamA; }
    public int getScoreTeamB() { return scoreTeamB; }
    public void setScoreTeamB(int scoreTeamB) { this.scoreTeamB = scoreTeamB; }
    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public int getTeamAId() { return teamAId; }
    public void setTeamAId(int teamAId) { this.teamAId = teamAId; }
    public int getTeamBId() { return teamBId; }
    public void setTeamBId(int teamBId) { this.teamBId = teamBId; }

    public String getTeamAName() { return teamAName; }
    public void setTeamAName(String teamAName) { this.teamAName = teamAName; }
    public String getTeamBName() { return teamBName; }
    public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRivalry() { return isRivalry; }
    public void setRivalry(boolean rivalry) { isRivalry = rivalry; }
}
