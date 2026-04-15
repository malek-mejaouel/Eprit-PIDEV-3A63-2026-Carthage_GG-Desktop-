package com.carthagegg.models;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String email;
    private String password;
    private String roles; // Store as JSON string or List<String>
    private String username;
    private String firstName;
    private String lastName;
    private String avatar;
    private String googleId;
    private boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime bannedUntil;
    private String banReason;
    private boolean verified;
    private String verifiedRoleBadge;
    private LocalDateTime verificationDate;

    // Default constructor
    public User() {}

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getVerifiedRoleBadge() { return verifiedRoleBadge; }
    public void setVerifiedRoleBadge(String verifiedRoleBadge) { this.verifiedRoleBadge = verifiedRoleBadge; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    @Override
    public String toString() {
        return username != null ? username : email;
    }
}
