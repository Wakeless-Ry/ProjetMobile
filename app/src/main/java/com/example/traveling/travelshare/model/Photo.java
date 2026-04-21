package com.example.traveling.travelshare.model;

import java.util.List;

public class Photo {
    private String id;
    private String imageUrl;
    private String title;
    private String description;
    private PhotoLocation location;
    private String date;
    private String period;
    private List<String> comments;
    private String directions;
    private Author author;
    private List<String> tags;
    private String locationType; // nature, shop, street, monument, restaurant, other
    private int likes;
    private boolean isLiked;
    private boolean isPublic;
    private String createdAt;

    public Photo(String id, String imageUrl, String title, String description,
                       PhotoLocation location, String date, String period,
                       List<String> comments, String directions, Author author,
                       List<String> tags, String locationType, int likes,
                       boolean isLiked, boolean isPublic, String createdAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.period = period;
        this.comments = comments;
        this.directions = directions;
        this.author = author;
        this.tags = tags;
        this.locationType = locationType;
        this.likes = likes;
        this.isLiked = isLiked;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PhotoLocation getLocation() { return location; }
    public String getDate() { return date; }
    public String getPeriod() { return period; }
    public List<String> getComments() { return comments; }
    public String getDirections() { return directions; }
    public Author getAuthor() { return author; }
    public List<String> getTags() { return tags; }
    public String getLocationType() { return locationType; }
    public int getLikes() { return likes; }
    public boolean isLiked() { return isLiked; }
    public boolean isPublic() { return isPublic; }
    public String getCreatedAt() { return createdAt; }

    public void setLikes(int likes) { this.likes = likes; }
    public void setLiked(boolean liked) { isLiked = liked; }
}