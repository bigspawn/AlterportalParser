package ru.bigspawn.parser;

import static ru.bigspawn.parser.Main.logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ru.bigspawn.parser.bot.MyBot;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Worker implements Runnable {

  public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
  private static final String INSERT_NEWS =
      "INSERT INTO news_test "
          + "(title, id_news_type, date, gender, format, country, playlist, download_url, image_url) "
          + "VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_NEWS = "SELECT * FROM news_test WHERE title = ? AND id_news_type = ?";

  private MyBot bot;
  private WebClient client = new WebClient();
  private int pageNumber = 1;
  private String url;
  private Connection connection;
  private boolean key;

  public Worker(MyBot bot) throws UnsupportedEncodingException {
    this.bot = bot;
    this.url = getPageURL();
    setOptions();
    createConnection();
  }

  private void createConnection() {
    try {
      Class.forName("org.postgresql.Driver");
      connection = DriverManager
          .getConnection("jdbc:postgresql://localhost:15432/alterportal_news", "bigspawn",
              "52169248");
    } catch (ClassNotFoundException | SQLException e) {
      logger.error(e, e);
    }
  }

  private String getPageURL() throws UnsupportedEncodingException {
    return Configs.getInstance().getURL()
        + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + "/";
  }

  private void setOptions() {
    client.getOptions().setCssEnabled(false);
    client.getOptions().setJavaScriptEnabled(false);
  }

  @Override
  public void run() {
    logger.info("Start worker");
    while (!Thread.currentThread().isInterrupted()) {
      try {
        HtmlPage page = client.getPage(url);
        List<HtmlElement> elements = page
            .getByXPath("//*[@id=\"dle-content\"]/table/tbody/tr/td/table");
        logger.info("Get " + elements.size() + " news from page № " + pageNumber);
        for (HtmlElement element : elements) {
          List<HtmlElement> titleTds = element.getElementsByAttribute("td", "class", "category");
          if (titleTds != null && !titleTds.isEmpty()) {
            String newsCategory = titleTds.get(0).asText().trim();
            Optional<NewsType> optional = Arrays
                .stream(NewsType.values())
                .filter(
                    x -> newsCategory.equals(x.getName()))
                .findFirst();
            if (optional.isPresent()) {
              NewsType type = optional.get();
              HtmlElement titleElement = element
                  .getElementsByAttribute("td", "class", "ntitle")
                  .get(0);
              String newsURL = titleElement.getElementsByTagName("a").get(0).getAttribute("href");
              page = client.getPage(newsURL);
              List<HtmlElement> newsElements = page.getByXPath("//*[@id=\"dle-content\"]");
              if (newsElements != null && !newsElements.isEmpty()) {
                HtmlElement newsElement = newsElements.get(0);


                News news = new News();
                news.setType(type);
                news.setTitle(getNewsTitle(newsElement));

                HtmlElement newsBodyHtmlElement = (HtmlElement) newsElement.getByXPath("//div[contains(@id, \"news-id\")]").get(0);
                ArrayList<String> lines = getNewsAsStringArray(newsBodyHtmlElement);
                news.setGenre(getNewsTag(lines, "Стиль", "Жанр"));
                if (type != NewsType.Concerts) {
                  news.setCountry(getNewsTag(lines, "Страна", "Родина"));
                  news.setFormat(getNewsTag(lines, "Формат", "Качество"));
                }
                news.setPlaylist(getTrackList(lines));
                List<HtmlElement> aElements = newsBodyHtmlElement.getElementsByTagName("a");
                if (aElements != null && !aElements.isEmpty()) {
                  String imageUrl = getImageUrl(aElements);
                  if (imageUrl == null || imageUrl.isEmpty()
                      || !imageUrl.contains("fastpic")
                      || !imageUrl.contains("radikal")) {
                    imageUrl = getImageSrc(newsBodyHtmlElement.getElementsByTagName("img"));
                  }
                  news.setImageURL(imageUrl);
                  news.setDownloadURL(getHref(aElements));
                }

                news.setDateTime(getDateTime(newsElement));
                logger.info("Create new item: " + news);
                if (checkIfNewAlreadyInDatabase(news)) {
                  break;
                } else {
                  sendNewsToChannel(news);
                }
                logger.info("Sleep 10 seconds");
                TimeUnit.SECONDS.sleep(10);
              }
            }
          }
        }
        if (key) {
          pageNumber = 1;
          url = getPageURL();
          key = false;
          logger.info("Has not new news. Sleep 30 minutes");
          TimeUnit.MINUTES.sleep(30);
        } else {
          pageNumber++;
          url = getPageURL();
        }
      } catch (InterruptedException | IOException e) {
        logger.error(e, e);
      }
    }
    logger.info("Stop worker");
  }

  private ArrayList<String> getNewsAsStringArray(HtmlElement newsBodyHtmlElement) {
    String newsBody = newsBodyHtmlElement.asText();
    ArrayList<String> lines = new ArrayList<>(Arrays.asList(newsBody.split("\r\n")));
    lines.removeAll(Collections.singleton(""));
    return lines;
  }


  private String getNewsTag(ArrayList<String> lines, String firstTag, String secondTag) {
    StringBuilder formatBuilder = new StringBuilder();
    findNewsTag(lines, formatBuilder, firstTag);
    if (formatBuilder.length() == 0) {
      findNewsTag(lines, formatBuilder, secondTag);
    }
    return replaceColons(formatBuilder.toString());
  }

  private void findNewsTag(ArrayList<String> lines, StringBuilder countryBuilder, String tag) {
    Optional<String> country = lines.stream().filter(
        line -> StringUtils.containsIgnoreCase(line, tag)).findFirst();
    country.ifPresent(countryBuilder::append);
  }

  private String getNewsTitle(HtmlElement newsElement) {
    return ((HtmlElement) newsElement
        .getByXPath("//table/tbody/tr/td/table/tbody/tr/td[@class=\"ntitle\"]").get(0))
        .getTextContent();
  }

  private String replaceColons(String str) {
    return str.replace(":: ::", ":").replace("::", "").trim();
  }

  private DateTime getDateTime(HtmlElement newsElement) {
    DateTime dateTime = DateTime.now();
    List<HtmlElement> commentElements = newsElement.getByXPath(
        "//*[@id=\"dle-content\"]/table/tbody/tr/td/div[@class=\"slink1\"]");
    if (commentElements != null && !commentElements.isEmpty()) {
      String date = commentElements.get(0).getTextContent().trim();
      if (StringUtils.contains(date, "Вчера")) {
        dateTime = dateTime.minusDays(1);
      } else if (!StringUtils.contains(date, "Сегодня")) {
        Pattern pattern = Pattern.compile("\\|\\s.*");
        Matcher matcher = pattern.matcher(date);
        if (matcher.find()) {
          date = matcher.group();
          date = date.replace("|", "").trim();
          dateTime = formatter.parseDateTime(date);
        }
      }
    }
    return dateTime;
  }

  private void sendNewsToChannel(News news) {
    try {
      bot.sendNewsToChanel(news, Configs.getInstance().getTELEGRAM_CHANEL());
    } catch (Exception e) {
      logger.error(e, e);
    }
  }

  private boolean checkIfNewAlreadyInDatabase(News news) {
    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    try {
      ps = connection.prepareStatement(SELECT_NEWS);
      ps.setString(1, news.getTitle());
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        ps2 = connection.prepareStatement(INSERT_NEWS);
        ps2.setString(1, news.getTitle());
        ps2.setString(2, news.getType().getName());
        ps2.setTimestamp(3, new Timestamp(news.getDateTime().getMillis()));
        ps2.setString(4, news.getGenre());
        ps2.setString(5, news.getFormat());
        ps2.setString(6, news.getCountry());
        ps2.setString(7, news.getPlaylist());
        ps2.setString(8, news.getDownloadURL());
        ps2.setString(9, news.getImageURL());
        ps2.execute();
        logger.info("Add news: " + news.getTitle() + " - to DB");
      } else {
        key = true;
        return true;
      }
    } catch (SQLException e) {
      logger.error(e, e);
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          logger.error(e, e);
        }
      }
      if (ps2 != null) {
        try {
          ps2.close();
        } catch (SQLException e) {
          logger.error(e, e);
        }
      }
    }
    return false;
  }


  private String getTrackList(ArrayList<String> lines) {
    StringBuilder tracks = new StringBuilder("");
    for (int i = 0; i < lines.size(); i++) {
      if (StringUtils.contains(lines.get(i), "Треклист")
          || StringUtils.contains(lines.get(i), "Tracklist")) {
        for (int j = i + 1; j < lines.size(); j++) {
          String track = lines.get(j).trim();
          if (Character.isDigit(track.charAt(0))) {
            tracks.append(track);
            if (j < lines.size() - 1 && Character.isDigit(lines.get(j + 1).charAt(0))) {
              tracks.append("\n");
            } else {
              break;
            }
          }
        }
        break;
      }
    }
    return tracks.toString();
  }

  private String getImageUrl(List<HtmlElement> aElements) {
    List<HtmlElement> imageElements = aElements.get(0).getElementsByTagName("img");
    return getImageSrc(imageElements);
  }

  private String getImageSrc(List<HtmlElement> imageElements) {
    if (imageElements != null && !imageElements.isEmpty()) {
      HtmlElement imageElement = imageElements.get(0);
      if (imageElement != null) {
        return imageElement.getAttribute("src");
      }
    }
    return "";

  }

  private String getHref(List<HtmlElement> elements) {
    HtmlElement downloadElement = elements.get(elements.size() - 1);
    if (downloadElement != null) {
      return downloadElement.getAttribute("href");
    }
    return "";
  }
}
