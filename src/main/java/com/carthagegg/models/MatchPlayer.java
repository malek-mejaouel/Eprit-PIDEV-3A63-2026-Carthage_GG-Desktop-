package com.carthagegg.models;

public class MatchPlayer {
    private int id;
    private String role;
    private int matchId;
    private int userId;
    private int teamId;

    public MatchPlayer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
}
