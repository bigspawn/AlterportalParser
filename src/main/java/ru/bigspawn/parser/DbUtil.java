package ru.bigspawn.parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.bigspawn.parser.entity.News;

public class DbUtil {

  private static final String SELECT_NEWS =
      String.format("SELECT * FROM %s WHERE lower(title) = lower(?)",
          Configuration.getInstance().getDbName());
  private static final String INSERT_NEWS = String.format(
      "INSERT INTO %s (title, id_news_type, date, gender, format, country, playlist, download_url, image_url, page_url) "
          + "VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?)",
      Configuration.getInstance().getDbName());

  private static final Logger logger = LogManager.getLogger(DbUtil.class);

  private Connection connection;

  public DbUtil() throws SQLException {
    connection = DriverManager.getConnection(
        Configuration.getInstance().getDbUrl(),
        Configuration.getInstance().getDbUser(),
        Configuration.getInstance().getDbPassword()
    );
  }

  public boolean isPosted(News news) {
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
    return false;
  }

  public boolean insetToDatabase(News news) {
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

}
