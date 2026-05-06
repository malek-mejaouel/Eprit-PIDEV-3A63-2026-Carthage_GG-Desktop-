package com.carthagegg.models;

public class LeaderboardEntry {
    private int rank;
    private int teamId;
    private String teamName;
    private int wins;
    private int losses;
    private int draws;
    private int points;
    private double winRate;

    public LeaderboardEntry() {}

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
}
