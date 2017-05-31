package ru.bigspawn.parser;

import org.joda.time.DateTime;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class News {
    private String title;
    private String category;
    private String text;
    private DateTime dateTime;
    private String author;
    private String genre;
    private String format;
    private String country;
    private String playlist;
    private String downloadURL;
    private String imageURL;

    public News(String title, String category) {
        this.title = title;
        this.category = category;
    }

    public News(String title, String category, String text, DateTime dateTime, String author,
                String genre, String format, String country, String playlist, String downloadURL, String imageURL) {
        this.title = title;
        this.category = category;
        this.text = text;
        this.dateTime = dateTime;
        this.author = author;
        this.genre = genre;
        this.format = format;
        this.country = country;
        this.playlist = playlist;
        this.downloadURL = downloadURL;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", dateTime=" + dateTime +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", format='" + format + '\'' +
                ", country='" + country + '\'' +
                ", playlist='" + playlist + '\'' +
                ", downloadURL='" + downloadURL + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }
}
