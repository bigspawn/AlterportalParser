package ru.bigspawn.parser;


import java.sql.SQLException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.bigspawn.parser.bot.Bot;
import ru.bigspawn.parser.entity.News;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class QueueWorker implements Runnable {

  public static final int ZERO_POSTED_NEWS = 0;
  public static final BlockingDeque<News> queue = new LinkedBlockingDeque<>();
  private static final int SLEEPING_TIME = Configuration.getInstance().getSleepingTime();
  private static final int SLEEPING_TIME_FOR_NEWS = Configuration.getInstance()
      .getSleepingTimeForNews();
  private static final int MAX_REPEATED_NEWS = Configuration.getInstance().getMaxRepeatedNews();
  private static final String TELEGRAM_CHANEL = Configuration.getInstance().getTelegramChanel();
  private static final Logger logger = LogManager.getLogger(QueueWorker.class);
  private final DbUtil dbUtil;
  private Bot bot;
  private int postedNewsCount;

  public QueueWorker(Bot bot) throws SQLException {
    this.bot = bot;
    this.dbUtil = new DbUtil();
  }

  @Override
  public void run() {
    logger.info("Start Queue Worker: " + Thread.currentThread().getName());
    while (!Thread.currentThread().isInterrupted()) {
      try {
        sendNews();
      } catch (InterruptedException e) {
        logger.error(e, e);
      }
    }
  }

  private void sendNews() throws InterruptedException {
    boolean isNewsRepeatedMaxTimes = false;
    if (queue.size() > 0) {
      for (int i = 0; i < queue.size(); i++) {
        News news = queue.peek();
        if (news != null) {
          logger.info("Get news: " + news.getTitle());
          if (dbUtil.isPosted(news)) {
            if (++postedNewsCount >= MAX_REPEATED_NEWS) {
              isNewsRepeatedMaxTimes = true;
              logger.info("News was repeated " + postedNewsCount + " times!");
              break;
            }
            queue.remove();
          } else {
            if (dbUtil.insetToDatabase(news)) {
              logger.info("Post news:" + news.getTitle());
              //todo: доделать - если не отправилось но есть в БД - надо или перепарсить или заново оправить
              bot.sendNewsToChannel(news, TELEGRAM_CHANEL, logger);

              queue.remove();
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
        sleep(String.format("News repeated max times! {%d}", MAX_REPEATED_NEWS));
      }
    } else {
      logger.info("Queue is empty!");
      TimeUnit.SECONDS.sleep(10);
    }
  }

  private void sleep(String message) {
    try {
      logger.info(String.format("%s. Sleep %d minutes", message, SLEEPING_TIME));
      postedNewsCount = ZERO_POSTED_NEWS;
      TimeUnit.MINUTES.sleep(SLEEPING_TIME);
    } catch (InterruptedException e) {
      logger.error(e, e);
    }
  }


  @Override
  public String toString() {
    return String.format("Worker{bot=%s, postedNewsCount=%d}", bot, postedNewsCount);
  }
}
