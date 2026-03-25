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

    public Comment() {}

    public int getCommentaireId() { return commentaireId; }
    public void setCommentaireId(int commentaireId) { this.commentaireId = commentaireId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
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
