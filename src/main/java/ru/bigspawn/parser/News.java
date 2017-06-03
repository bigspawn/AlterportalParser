package ru.bigspawn.parser;

import org.joda.time.DateTime;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class News {
    private String title;
    private NewsType type;
    private DateTime dateTime;
    private String genre;
    private String format;
    private String country;
    private String playlist;
    private String downloadURL;
    private String imageURL;

    public News(String title, NewsType type) {
        this.title = title;
        this.type = type;
    }

    public News(String title, NewsType type, DateTime dateTime, String genre, String format,
                String country, String playlist, String downloadURL, String imageURL) {
        this.title = title;
        this.type = type;
        this.dateTime = dateTime;
        this.genre = genre;
        this.format = format;
        this.country = country;
        this.playlist = playlist;
        this.downloadURL = downloadURL;
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", dateTime=" + dateTime +
                ", genre='" + genre + '\'' +
                ", format='" + format + '\'' +
                ", country='" + country + '\'' +
                ", playlist='" + playlist + '\'' +
                ", downloadURL='" + downloadURL + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }
}
