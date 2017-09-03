package ru.bigspawn.parser.entity;

import org.joda.time.DateTime;
import ru.bigspawn.parser.Constant;

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

  public News() {
  }

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

  public String getTitle() {
    return title;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public String getTextForMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append(title).append('\n')
        .append("Категория: ").append(type).append('\n');
    if (type == NewsType.News) {
      builder.append("\n");
    }
    addStringIfNotNullOrEmpty(builder, genre);
    addStringIfNotNullOrEmpty(builder, format);
    addStringIfNotNullOrEmpty(builder, country);
    builder.append("Дата: ").append(Constant.FORMATTER.print(dateTime)).append('\n');
    addStringIfNotNullOrEmpty(builder, playlist, "Плейлист: \n");
    return builder.toString();
  }

  private void addStringIfNotNullOrEmpty(StringBuilder builder, String text) {
    if (text != null && !text.isEmpty()) {
      builder.append(text).append('\n');
    }
  }

  private void addStringIfNotNullOrEmpty(StringBuilder builder, String text, String preText) {
    if (text != null && !text.isEmpty()) {
      builder.append(preText).append(text).append('\n');
    }
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

    if (title != null ? !title.equals(news.title) : news.title != null) {
      return false;
    }
    if (type != news.type) {
      return false;
    }
    if (dateTime != null ? !dateTime.toDateMidnight().isEqual(news.dateTime.toDateMidnight())
        : news.dateTime != null) {
      return false;
    }
    if (genre != null ? !genre.equals(news.genre) : news.genre != null) {
      return false;
    }
    if (format != null ? !format.equals(news.format) : news.format != null) {
      return false;
    }
    if (country != null ? !country.equals(news.country) : news.country != null) {
      return false;
    }
    if (playlist != null ? !playlist.equals(news.playlist) : news.playlist != null) {
      return false;
    }
    if (downloadURL != null ? !downloadURL.equals(news.downloadURL) : news.downloadURL != null) {
      return false;
    }
    return imageURL != null ? imageURL.equals(news.imageURL) : news.imageURL == null;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + dateTime.hashCode();
    result = 31 * result + genre.hashCode();
    result = 31 * result + format.hashCode();
    result = 31 * result + country.hashCode();
    result = 31 * result + playlist.hashCode();
    result = 31 * result + (downloadURL != null ? downloadURL.hashCode() : 0);
    result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
    return result;
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
        ", playlist='" + playlist.replace('\n', ',') + '\'' +
        ", downloadURL='" + downloadURL + '\'' +
        ", imageURL='" + imageURL + '\'' +
        '}';
  }
}
