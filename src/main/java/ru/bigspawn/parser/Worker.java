package ru.bigspawn.parser;

import static ru.bigspawn.parser.Main.logger;

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
import ru.bigspawn.parser.bot.Bot;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Worker implements Runnable {

  private static final String INSERT_NEWS =
      " INSERT INTO " + Configuration.getInstance().getDbName() +
          " (title, id_news_type, date, gender, format, country, playlist, download_url, image_url) "
          +
          " VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_NEWS =
      " SELECT * FROM " + Configuration.getInstance().getDbName() +
          " WHERE title = ? AND id_news_type = (SELECT id_news_type FROM news_type WHERE name = ?)";

  private static final int MAX_REPEAT_NEWS = 10;

  private Connection connection;
  private Parser parser;
  private Bot bot;
  private int newsCounter;
  private int pageNumber = 1;
  private boolean key;
  private String telegramChanel;


  public Worker(Parser parser, Bot bot) throws UnsupportedEncodingException {
    this.parser = parser;
    this.bot = bot;
    telegramChanel = Configuration.getInstance().getTelegramChanel();
    createConnection();
  }

  private void createConnection() {
    try {
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection(
          Configuration.getInstance().getDbUrl(),
          Configuration.getInstance().getDbUser(),
          Configuration.getInstance().getDbPasswd());
    } catch (ClassNotFoundException | SQLException e) {
      logger.error(e, e);
    }
  }

  @Override
  public void run() {
    logger.info("Start worker");
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
            key = pageNumber != 1 || newsCounter >= MAX_REPEAT_NEWS;
            logger.info("Are we still in a first page? - " + !key
                + " - And count is " + newsCounter);
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
    logger.info("All news repeated on page: " + pageNumber);
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
      ps.setString(2, news.getType().getName());
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

  private void sendToChannel(News news) {
    try {
      logger.info("Try send news: " + news);
      bot.sendNewsToChanel(news, telegramChanel);
    } catch (Exception e) {
      logger.error(e, e);
    }
  }
}
