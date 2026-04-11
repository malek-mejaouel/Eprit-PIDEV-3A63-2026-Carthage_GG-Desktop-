package com.carthagegg.models;

import java.time.LocalDateTime;

public class News {
    private int newsId;
    private String title;
    private String content;
    private String image;
    private LocalDateTime publishedAt;
    private String category;

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
}
