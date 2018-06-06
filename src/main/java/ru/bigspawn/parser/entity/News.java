package ru.bigspawn.parser.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import ru.bigspawn.parser.Constant;
import ru.bigspawn.parser.Utils;

/**
 * Created by bigspawn on 30.05.2017.
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode(exclude = "dateTime, downloadURL, imageURL")
@NoArgsConstructor
@AllArgsConstructor
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

  public News(String title, NewsType type) {
    this.title = title;
    this.type = type;
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

}
