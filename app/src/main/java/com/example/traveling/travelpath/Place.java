package com.example.traveling.travelpath;

import java.util.ArrayList;
import java.util.List;

public class Place {
    private String id;
    private String name;
    private List<String> tags;
    private double price;
    private double latitude;
    private double longitude;
    private double usualTimeSpentHours;
    private boolean selected;

    public Place() {
        this.tags = new ArrayList<>();
        this.selected = false;
    }

    public Place(String id, String name, double price, double latitude, double longitude, double usualTimeSpentHours) {
        this.id = id;
        this.name = name;
        this.tags = new ArrayList<>();
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.usualTimeSpentHours = usualTimeSpentHours;
        this.selected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getUsualTimeSpentHours() {
        return usualTimeSpentHours;
    }

    public void setUsualTimeSpentHours(double usualTimeSpentHours) {
        this.usualTimeSpentHours = usualTimeSpentHours;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFormattedLocation() {
        return String.format("%.4f, %.4f", latitude, longitude);
    }

    public String getFormattedPrice() {
        return String.format("%.2f €", price);
    }

    public String getFormattedTimeSpent() {
        if (usualTimeSpentHours < 1) {
            return String.format("%.0f min", usualTimeSpentHours * 60);
        } else if (usualTimeSpentHours == (int) usualTimeSpentHours) {
            return String.format("%.0f h", usualTimeSpentHours);
        } else {
            return String.format("%.1f h", usualTimeSpentHours);
        }
    }

    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", price=" + price +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", usualTimeSpentHours=" + usualTimeSpentHours +
                ", selected=" + selected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Place place = (Place) o;

        return id.equals(place.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
