package com.carthagegg.models;

import java.time.LocalDateTime;

public class Stream {
    private int streamId;
    private String title;
    private String description;
    private String platform; // twitch, youtube
    private String channelName;
    private String youtubeVideoId;
    private String thumbnail;
    private boolean live;
    private int viewerCount;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stream() {}

    public int getStreamId() { return streamId; }
    public void setStreamId(int streamId) { this.streamId = streamId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }
    public int getViewerCount() { return viewerCount; }
    public void setViewerCount(int viewerCount) { this.viewerCount = viewerCount; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
