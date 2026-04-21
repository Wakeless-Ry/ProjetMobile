package com.example.traveling.travelshare.model;

public class PhotoLocation {
    private String name;
    private double lat;
    private double lng;
    private boolean isApproximate;

    public PhotoLocation(String name, double lat, double lng, boolean isApproximate) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.isApproximate = isApproximate;
    }

    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public boolean isApproximate() { return isApproximate; }
}
