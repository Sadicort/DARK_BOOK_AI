package darkbook.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VideoData {

    // Identificación
    private String id;
    private String videoUrl;
    private LocalDateTime collectedAt;

    // Autor
    private String username;
    private String displayName;

    // Contenido
    private String description;
    private List<String> hashtags = new ArrayList<>();

    // Métricas
    private String likes;
    private String comments;
    private String shares;
    private String favorites;

    // Audio
    private String audioName;

    // Archivo
    private String screenshotPath;

    // Scanner
    private int watchTime;
    private String category = "UNKNOWN";

    public VideoData() {
        collectedAt = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public LocalDateTime getCollectedAt() { return collectedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getHashtags() { return hashtags; }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getLikes() { return likes; }
    public void setLikes(String likes) { this.likes = likes; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getShares() { return shares; }
    public void setShares(String shares) { this.shares = shares; }

    public String getFavorites() { return favorites; }
    public void setFavorites(String favorites) { this.favorites = favorites; }

    public String getAudioName() { return audioName; }
    public void setAudioName(String audioName) { this.audioName = audioName; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public int getWatchTime() { return watchTime; }
    public void setWatchTime(int watchTime) { this.watchTime = watchTime; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

}