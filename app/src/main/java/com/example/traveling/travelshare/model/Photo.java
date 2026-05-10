package com.example.traveling.travelshare.model;

import java.util.ArrayList;
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
    private String locationType;
    private int likes;
    private boolean isLiked;
    private boolean isPublic;
    private String createdAt;
    private String groupId;

    public Photo(String id, String imageUrl, String title, String description,
                       PhotoLocation location, String date, String period,
                       List<String> comments, String directions, Author author,
                       List<String> tags, String locationType, int likes,
                       boolean isLiked, boolean isPublic, String createdAt) {
        this(id, imageUrl, title, description, location, date, period, comments, 
             directions, author, tags, locationType, likes, isLiked, isPublic, createdAt, null);
    }

    public Photo(String id, String imageUrl, String title, String description,
                 PhotoLocation location, String date, String period,
                 List<String> comments, String directions, Author author,
                 List<String> tags, String locationType, int likes,
                 boolean isLiked, boolean isPublic, String createdAt, String groupId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.period = period;
        this.comments = new ArrayList<>(comments);
        this.directions = directions;
        this.author = author;
        this.tags = tags;
        this.locationType = locationType;
        this.likes = likes;
        this.isLiked = isLiked;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.groupId = groupId;
    }

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
    public String getGroupId() { return groupId; }

    public void setLikes(int likes) { this.likes = likes; }
    public void setLiked(boolean liked) { isLiked = liked; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setComments(List<String> comments) { this.comments = comments; }
}
