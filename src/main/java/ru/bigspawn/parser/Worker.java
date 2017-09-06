package ru.bigspawn.parser;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.bigspawn.parser.bot.Bot;
import ru.bigspawn.parser.entity.News;
import ru.bigspawn.parser.parser.Parser;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Worker implements Runnable {

  private static final String SELECT_NEWS =
      "SELECT * FROM " + Configuration.getInstance().getDbName() + " WHERE title = ?";
  private static final String INSERT_NEWS =
      " INSERT INTO " + Configuration.getInstance().getDbName()
          + " (title, id_news_type, date, gender, format, country, playlist, download_url, image_url)"
          + " VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";

  private Parser parser;
  private Bot bot;
  private Logger logger;
  private Connection connection;
  private String telegramChanel;
  private int newsCounter;
  private int pageNumber = 1;
  private int maxRepeatedNews;
  private boolean key;

  public Worker(Parser parser, Bot bot, String loggerName) throws UnsupportedEncodingException {
    this.parser = parser;
    this.bot = bot;
    this.logger = LogManager.getLogger(loggerName);
    telegramChanel = Configuration.getInstance().getTelegramChanel();
    maxRepeatedNews = Configuration.getInstance().getMaxRepeatedNews();
    createConnection();
  }

  private void createConnection() {
    try {
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection(
          Configuration.getInstance().getDbUrl(),
          Configuration.getInstance().getDbUser(),
          Configuration.getInstance().getDbPassword());
    } catch (ClassNotFoundException | SQLException e) {
      logger.error(e, e);
    }
  }

  @Override
  public void run() {
    logger.info("Start worker: " + Thread.currentThread().getName());
    while (!Thread.currentThread().isInterrupted()) {
      try {
        List<News> news = parser.parse(pageNumber);
        for (News article : news) {
          if (!ifAlreadyPosted(article)) {
            insetToDatabase(article);
            sendToChannel(article);
            int sleepingTimeForNews = Configuration.getInstance().getSleepingTimeForNews();
            logger.info("Sleep " + sleepingTimeForNews + " seconds");
            TimeUnit.SECONDS.sleep(sleepingTimeForNews);
          } else {
            newsCounter++;
            key = pageNumber != 1 || newsCounter >= maxRepeatedNews;
            if (key) {
              break;
            }
          }
        }
        if (key) {
          sleep();
        } else {
          pageNumber++;
        }
      } catch (InterruptedException | IOException e) {
        logger.error(e, e);
      }
    }
    logger.info("Stop worker");
  }

  private void sleep() throws UnsupportedEncodingException, InterruptedException {
    logger.info("All news on page: " + pageNumber + " was repeated!");
    pageNumber = 1;
    newsCounter = 0;
    key = false;
    int sleepingTime = Configuration.getInstance().getSleepingTime();
    logger.info("Waiting for new news. Sleep " + sleepingTime + " minutes");
    TimeUnit.MINUTES.sleep(sleepingTime);
  }

  private boolean ifAlreadyPosted(News news) {
    try (PreparedStatement ps = connection.prepareStatement(SELECT_NEWS)) {
      ps.setString(1, news.getTitle());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        logger.info("News '" + news.getTitle() + "' is already posted!");
        return true;
      }
    } catch (SQLException e) {
      logger.error(e, e);
    }
    return false;
  }

  private void insetToDatabase(News news) {
    try (PreparedStatement ps = connection.prepareStatement(INSERT_NEWS)) {
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
    }
  }

  private synchronized void sendToChannel(News news) {
    try {
      logger.info("Try send news: " + news);
      bot.sendNewsToChanel(news, telegramChanel);
    } catch (Exception e) {
      logger.error(e, e);
    }
  }
}
