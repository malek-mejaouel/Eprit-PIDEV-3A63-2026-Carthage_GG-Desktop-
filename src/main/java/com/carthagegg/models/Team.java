package com.carthagegg.models;

import java.time.LocalDate;

public class Team {
    private int teamId;
    private String teamName;
    private String logo;
    private LocalDate creationDate;
    private int userId; // Captain
    private String description;

    public Team() {}

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() { return teamName; }
}
