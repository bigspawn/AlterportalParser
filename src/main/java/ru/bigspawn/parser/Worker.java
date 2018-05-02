package ru.bigspawn.parser;


import java.io.IOException;
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
      String.format("SELECT * FROM %s WHERE lower(title) = lower(?)",
          Configuration.getInstance().getDbName());
  private static final String INSERT_NEWS = String.format(
      "INSERT INTO %s (title, id_news_type, date, gender, format, country, playlist, download_url, image_url, page_url) "
          + "VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?)",
      Configuration.getInstance().getDbName());
  public static final int FIRST_PAGE = 1;
  public static final int ZERO_POSTED_NEWS = 0;

  private Parser parser;
  private Bot bot;
  private Logger logger;
  private Connection connection;
  private int postedNewsCount;
  private int pageNumber = 1;

  public Worker(Parser parser, Bot bot, String loggerName) {
    this.parser = parser;
    this.bot = bot;
    this.logger = LogManager.getLogger(loggerName);
  }

  private void createConnection() throws SQLException {
    connection = DriverManager.getConnection(
        Configuration.getInstance().getDbUrl(),
        Configuration.getInstance().getDbUser(),
        Configuration.getInstance().getDbPassword()
    );
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
      } catch (SQLException e) {
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
        if (article != null) {
          if (isPosted(article)) {
            if (pageNumber != 1 || ++postedNewsCount >= MAX_REPEATED_NEWS) {
              isNewsRepeatedMaxTimes = true;
              logger.info("News was repeated " + postedNewsCount + " times!");
              break;
            }
          } else {
            if (insetToDatabase(article)) {
              //todo: доделать - если не отправилось но есть в БД - надо или перепарсить или заново оправить
              sendToChannel(article);
              logger.info("Sleep " + SLEEPING_TIME_FOR_NEWS + " seconds");
              TimeUnit.SECONDS.sleep(SLEEPING_TIME_FOR_NEWS);
            } else {
              logger.info("False insert into db - Sleep " + SLEEPING_TIME + " minutes");
              TimeUnit.MINUTES.sleep(SLEEPING_TIME);
            }
          }
        }
      }
      if (isNewsRepeatedMaxTimes) {
        sleep("News repeated");
      } else {
        pageNumber++;
        logger.debug("Go to next page: " + pageNumber);
      }
    } else {
      sleep("Waiting for new news.");
    }
  }

  private void sleep(String message) {
    try {
      logger.info(String.format("%s. Sleep %d minutes", message, SLEEPING_TIME));
      pageNumber = FIRST_PAGE;
      postedNewsCount = ZERO_POSTED_NEWS;
      logger.debug(this);
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
        logger.info(String.format("News '%s' is already posted!", news.getTitle()));
        return true;
      }
    } catch (SQLException e) {
      logger.error(e, e);
    }
//    logger.debug("News is new: " + news);
    return false;
  }

  private boolean insetToDatabase(News news) {
    logger.debug("Try insert new into db: " + news);
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
      ps.setString(10, news.getPageURL());
      ps.execute();
      return true;
    } catch (SQLException e) {
      logger.error(e, e);
    }
    return false;
  }

  private void sendToChannel(News news) {
    try {
      logger.info("Send news: " + news);
      bot.sendNewsToChannel(news, TELEGRAM_CHANEL, logger);
    } catch (Exception e) {
      logger.error(e, e);
    }
  }

  @Override
  public String toString() {
    return String.format(
        "Worker{parser=%s, bot=%s, logger=%s, connection=%s, postedNewsCount=%d, pageNumber=%d}",
        parser, bot, logger, connection, postedNewsCount, pageNumber);
  }
}
