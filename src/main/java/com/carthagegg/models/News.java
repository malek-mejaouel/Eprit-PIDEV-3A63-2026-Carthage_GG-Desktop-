package com.carthagegg.models;

import java.time.LocalDateTime;

public class News {
    private int newsId;
    private String title;
    private String content;
    private String image;
    private LocalDateTime publishedAt;
    private String category;
    private String url;
    private int viewCount;
    private int commentCount;
    private int totalUpvotes;

    public News() {}

    public int getNewsId() { return newsId; }
    public void setNewsId(int newsId) { this.newsId = newsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public int getTotalUpvotes() { return totalUpvotes; }
    public void setTotalUpvotes(int totalUpvotes) { this.totalUpvotes = totalUpvotes; }
}
//MEDAZIZ
