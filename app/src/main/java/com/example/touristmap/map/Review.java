package com.example.touristmap.map;

public class Review {
    private String author;
    private String comment;
    private float rating;
    private long timestamp;
    private String placeName;
    public Review() { }

    public Review(String author, String comment, float rating, String placeName) {
        this.author = author;
        this.comment = comment;
        this.rating = rating;
        this.placeName = placeName;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getAuthor() { return author; }
    public String getComment() { return comment; }
    public float getRating() { return rating; }
    public long getTimestamp() { return timestamp; }
    public String getPlaceName() {return placeName;}
}