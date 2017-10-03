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

  private static final int SLEEPING_TIME = Configuration.getInstance().getSleepingTime();
  private static final int SLEEPING_TIME_FOR_NEWS = Configuration.getInstance()
      .getSleepingTimeForNews();
  private static final int MAX_REPEATED_NEWS = Configuration.getInstance().getMaxRepeatedNews();
  private static final String TELEGRAM_CHANEL = Configuration.getInstance().getTelegramChanel();
  private static final String SELECT_NEWS =
      String.format("SELECT * FROM %s WHERE title = ?", Configuration.getInstance().getDbName());
  private static final String INSERT_NEWS = String.format(
      "INSERT INTO %s (title, id_news_type, date, gender, format, country, playlist, download_url, image_url) VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)",
      Configuration.getInstance().getDbName());

  private Parser parser;
  private Bot bot;
  private Logger logger;
  private Connection connection;
  private int newsCounter;
  private int pageNumber = 1;

  public Worker(Parser parser, Bot bot, String loggerName) throws UnsupportedEncodingException {
    this.parser = parser;
    this.bot = bot;
    this.logger = LogManager.getLogger(loggerName);
  }

  private void createConnection() throws SQLException, ClassNotFoundException {
    Class.forName("org.postgresql.Driver");
    connection = DriverManager.getConnection(
        Configuration.getInstance().getDbUrl(),
        Configuration.getInstance().getDbUser(),
        Configuration.getInstance().getDbPassword());
  }

  @Override
  public void run() {
    logger.info("Start worker: " + Thread.currentThread().getName());
    while (!Thread.currentThread().isInterrupted()) {
      try {
        if (connection == null) {
          createConnection();
        } else {
          sendNews(parser.parse(pageNumber));
        }
      } catch (InterruptedException | IOException e) {
        logger.error(e, e);
      } catch (SQLException | ClassNotFoundException e) {
        logger.error(e, e);
        sleep(e.getMessage());
      }
    }
    logger.info("Stop worker");
  }

  private void sendNews(List<News> news) throws InterruptedException {
    if (news != null && news.size() > 0) {
      boolean isNewsRepeatedMaxTimes = false;
      for (News article : news) {
        if (isPosted(article)) {
          if (pageNumber != 1 || newsCounter++ >= MAX_REPEATED_NEWS) {
            isNewsRepeatedMaxTimes = true;
            logger.info("News was repeated " + newsCounter + " times!");
            break;
          }
        } else {
          insetToDatabase(article);
          sendToChannel(article);
          logger.info("Sleep " + SLEEPING_TIME_FOR_NEWS + " seconds");
          TimeUnit.SECONDS.sleep(SLEEPING_TIME_FOR_NEWS);
        }
      }
      if (isNewsRepeatedMaxTimes) {
        sleep("News repeated");
      } else {
        pageNumber++;
      }
    } else {
      sleep("Waiting for new news.");
    }
  }

  private void sleep(String message) {
    try {
      logger.info(message + ". Sleep " + SLEEPING_TIME + " minutes");
      pageNumber = 1;
      newsCounter = 0;
      TimeUnit.MINUTES.sleep(SLEEPING_TIME);
    } catch (InterruptedException e) {
      logger.error(e, e);
    }
  }

  private boolean isPosted(News news) {
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
      logger.info("Try sendNews news: " + news);
      bot.sendNewsToChanel(news, TELEGRAM_CHANEL, logger);
    } catch (Exception e) {
      logger.error(e, e);
    }
  }

  @Override
  public String toString() {
    return "Worker{" + logger.getName() + '}';
  }
}
