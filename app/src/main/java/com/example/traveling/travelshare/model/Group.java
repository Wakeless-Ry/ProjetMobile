package com.example.traveling.travelshare.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private List<String> memberIds;
    private List<Photo> photos;

    public Group(String id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.memberIds = new ArrayList<>();
        this.photos = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getMemberIds() { return memberIds; }
    public List<Photo> getPhotos() { return photos; }
}
