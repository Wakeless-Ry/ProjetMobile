package com.example.traveling.travelpath;

import java.util.ArrayList;
import java.util.List;import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Place implements Parcelable {
    private String name;
    private List<String> tags;
    private double price;
    private double latitude;
    private double longitude;
    private double usualTimeSpentHours;
    private boolean selected;

    public Place() {
        this.tags = new ArrayList<>();
        this.selected = true;
    }

    protected Place(Parcel in) {
        name = in.readString();
        tags = in.createStringArrayList();
        price = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        usualTimeSpentHours = in.readDouble();
        selected = in.readByte() != 0;
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

    public double getPrice() {
        return price;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getUsualTimeSpentHours() {
        return usualTimeSpentHours;
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

    public double getDistance(Place other) {
        return Math.sqrt((this.latitude - other.latitude) * (this.latitude - other.latitude) + (this.longitude - other.longitude) * (this.longitude - other.longitude)) * 111;
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
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

        return name.equals(place.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int i) {
        dest.writeString(name);
        dest.writeStringList(tags);
        dest.writeDouble(price);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(usualTimeSpentHours);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) { return new Place(in); }
        @Override
        public Place[] newArray(int size) { return new Place[size]; }
    };
}
