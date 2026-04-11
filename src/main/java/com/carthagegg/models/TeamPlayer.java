package com.carthagegg.models;

public class TeamPlayer {
    private int id;
    private int teamId;
    private int userId;

    public TeamPlayer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
