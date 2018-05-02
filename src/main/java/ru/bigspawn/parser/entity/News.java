package ru.bigspawn.parser.entity;

import java.util.Objects;
import org.joda.time.DateTime;
import ru.bigspawn.parser.Constant;
import ru.bigspawn.parser.Utils;

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
  private String pageURL;

  public News() {
  }

  public News(String title, NewsType type) {
    this.title = title;
    this.type = type;
  }

  public News(String title, NewsType type, DateTime dateTime, String genre, String format,
      String country, String playlist, String downloadURL, String imageURL, String pageURL) {
    this.title = title;
    this.type = type;
    this.dateTime = dateTime;
    this.genre = genre;
    this.format = format;
    this.country = country;
    this.playlist = playlist;
    this.downloadURL = downloadURL;
    this.imageURL = imageURL;
    this.pageURL = pageURL;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public NewsType getType() {
    return type;
  }

  public void setType(NewsType type) {
    this.type = type;
  }

  public DateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(DateTime dateTime) {
    this.dateTime = dateTime;
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

  public String getPageURL() {
    return pageURL;
  }

  public void setPageURL(String pageURL) {
    this.pageURL = pageURL;
  }

  public String getTextForMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append(title).append('\n')
        .append("Категория: ").append(type).append('\n');
    Utils.appendIfNotNPE(builder, genre, format, country);
    builder.append("Дата: ").append(Constant.FORMATTER.print(dateTime)).append('\n');
    Utils.appendIfNotNPE(builder, "Плейлист: ", playlist);
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    News news = (News) o;
    return Objects.equals(title, news.title) &&
        type == news.type &&
        Objects.equals(dateTime, news.dateTime) &&
        Objects.equals(genre, news.genre) &&
        Objects.equals(format, news.format) &&
        Objects.equals(country, news.country) &&
        Objects.equals(playlist, news.playlist) &&
        Objects.equals(downloadURL, news.downloadURL) &&
        Objects.equals(imageURL, news.imageURL) &&
        Objects.equals(pageURL, news.pageURL);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(title, type, dateTime, genre, format, country, playlist, downloadURL, imageURL,
            pageURL);
  }

  @Override
  public String toString() {
    return String.format(
        "News{title='%s', type='%s', dateTime=%s, genre='%s', format='%s', country='%s', playlist='%s', downloadURL='%s', imageURL='%s', pageURL='%s'}",
        title, type, dateTime, genre, format, country, playlist.replace('\n', ','), downloadURL,
        imageURL, imageURL);
  }
}
