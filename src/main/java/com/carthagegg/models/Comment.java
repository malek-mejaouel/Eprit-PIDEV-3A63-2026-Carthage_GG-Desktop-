package com.carthagegg.models;

import java.time.LocalDateTime;

public class Comment {
    private int commentaireId;
    private String contenu;
    private LocalDateTime dateCommentaire;
    private String gifUrl;
    private int upvotes;
    private int downvotes;
    private int newsId;
    private int userId;
    private String username; // transient for display
    private String avatar; // transient for display
    private int parentId; // For nested comments: stored in content as __PARENT__{id}||{actual_content}

    public Comment() {}

    public int getCommentaireId() { return commentaireId; }
    public void setCommentaireId(int commentaireId) { this.commentaireId = commentaireId; }
    
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { 
        this.contenu = contenu;
        parseParentId();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRawContent() {
        if (parentId > 0) {
            return "__PARENT__" + parentId + "||" + contenu;
        }
        return contenu;
    }

    private void parseParentId() {
        if (contenu != null && contenu.startsWith("__PARENT__")) {
            try {
                int pipe = contenu.indexOf("||");
                if (pipe > 0) {
                    String idPart = contenu.substring(10, pipe);
                    this.parentId = Integer.parseInt(idPart);
                    this.contenu = contenu.substring(pipe + 2);
                }
            } catch (Exception e) {
                this.parentId = 0;
            }
        }
    }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public LocalDateTime getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(LocalDateTime dateCommentaire) { this.dateCommentaire = dateCommentaire; }
    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
    public int getNewsId() { return newsId; }
    public void setNewsId(int newsId) { this.newsId = newsId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
