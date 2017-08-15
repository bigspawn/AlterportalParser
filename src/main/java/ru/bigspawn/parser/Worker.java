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
import ru.bigspawn.parser.bot.Bot;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Worker implements Runnable {

  public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
  private static final String INSERT_NEWS =
      "INSERT INTO news "
          + "(title, id_news_type, date, gender, format, country, playlist, download_url, image_url) "
          + "VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_NEWS = "SELECT * FROM news WHERE title = ? AND id_news_type = (SELECT id_news_type FROM news_type WHERE name = ?)";

  private Bot bot;
  private WebClient client = new WebClient();
  private int pageNumber = 1;
  private String url;
  private Connection connection;
  private boolean key;
  private static final int MAX_REPEAT_NEWS = 10;
  private int newsCounter;

  public Worker(Bot bot) throws UnsupportedEncodingException {
    this.bot = bot;
    this.url = getPageURL();
    setOptions();
    createConnection();
  }

  private void createConnection() {
    try {
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection(
          Configs.getInstance().getDbUrl(),
          Configs.getInstance().getDbUser(),
          Configs.getInstance().getDbPasswd());
    } catch (ClassNotFoundException | SQLException e) {
      logger.error(e, e);
    }
  }

  private String getPageURL() throws UnsupportedEncodingException {
    return Configs.getInstance().getUrl()
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
        logger.info("Get " + elements.size() + " news from page " + pageNumber);
        for (HtmlElement element : elements) {
          List<HtmlElement> titleTds = element.getElementsByAttribute("td", "class", "category");
          if (titleTds != null && !titleTds.isEmpty()) {
            String newsCategory = titleTds.get(0).asText().trim();
            Optional<NewsType> optional = Arrays.stream(NewsType.values())
                .filter(x -> newsCategory.equals(x.getName()))
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

                HtmlElement newsBodyHtmlElement = (HtmlElement) newsElement
                    .getByXPath("//div[contains(@id, \"news-id\")]").get(0);
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
                if (!ifNewsAlreadyPosted(news)) {
                  insetNewsToDatabase(news);
                  sendNewsToChannel(news);
                  logger.info("Sleep 10 seconds");
                  TimeUnit.SECONDS.sleep(10);
                } else {
                  newsCounter++;
                  key = pageNumber != 1 || newsCounter >= MAX_REPEAT_NEWS;
                  logger.info("Are we still in a first page? - " + !key + " - And count is " + newsCounter);
                  if (key) {
                    break;
                  }
                }
              }
            }
          }
        }
        if (key) {
          sleep();
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

  private void sleep() throws UnsupportedEncodingException, InterruptedException {
    logger.info("All news repeated on page: " + pageNumber);
    pageNumber = 1;
    newsCounter = 0;
    url = getPageURL();
    key = false;
    logger.info("Waiting for new news. Sleep 1 hour");
    TimeUnit.HOURS.sleep(1);
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
    Optional<String> country = lines.stream()
        .filter(line -> StringUtils.containsIgnoreCase(line, tag))
        .findFirst();
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
      logger.info("Try send news: " + news);
      bot.sendNewsToChanel(news, Configs.getInstance().getTelegramChanel());
    } catch (Exception e) {
      logger.error(e, e);
    }
  }

  private boolean ifNewsAlreadyPosted(News news) {
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement(SELECT_NEWS);
      ps.setString(1, news.getTitle());
      ps.setString(2, news.getType().getName());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        logger.info("News '" + news.getTitle() + "' is already posted!");
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
    }
    return false;
  }

  private void insetNewsToDatabase(News news) {
    PreparedStatement ps = null;
    try {
      ps = connection.prepareStatement(INSERT_NEWS);
      ps.setString(1, news.getTitle());
      ps.setString(2, news.getType().getName());
      ps.setTimestamp(3, new Timestamp(news.getDateTime().getMillis()));
      ps.setString(4, news.getGenre());
      ps.setString(5, news.getFormat());
      ps.setString(6, news.getCountry());
      ps.setString(7, news.getPlaylist());
      ps.setString(8, news.getDownloadURL());
      ps.setString(9, news.getImageURL());
      ps.execute();
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
    }
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
