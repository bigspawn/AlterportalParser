package ru.bigspawn.parser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.bigspawn.parser.bot.Bot;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {

  public static final Logger logger = LogManager.getLogger(Main.class.getName());
  public static final ExecutorService executor = Executors
      .newFixedThreadPool(Configuration.getInstance().getThreads());

  private static final List<Thread> threads = new ArrayList<>();

  public static void main(String[] args) {
    try {
      logger.info("Start application");
      logger.info(System.getProperties().get("log4j.configurationFile"));

      Class.forName("org.postgresql.Driver");

      ApiContextInitializer.init();
      Bot bot = getTelegramBot();

      startWorkers(bot);
    } catch (ClassNotFoundException | TelegramApiRequestException | SQLException e) {
      logger.error(e, e);
    }
  }

  private static Bot getTelegramBot() throws TelegramApiRequestException {
    Bot bot = new Bot();

    if (Configuration.getInstance().isUseProxy()) {
      RequestConfig requestConfig = RequestConfig.copy(RequestConfig.custom().build())
          .setProxy(
              new HttpHost(
                  Configuration.getInstance().getProxyHost(),
                  Configuration.getInstance().getProxyPort()))
          .build();
      bot.getOptions().setRequestConfig(requestConfig);
    } else {
      logger.info("Not use proxy");
    }

    TelegramBotsApi botsApi = new TelegramBotsApi();
    botsApi.registerBot(bot);

    return bot;
  }

  private static void startWorkers(Bot bot) throws SQLException {
    List<String> urls = Configuration.getInstance().getUrls();
    for (String url : urls) {
      Thread thread = new Thread(new Worker(url), "Thread: " + Utils.getLoggerNameFromUrl(url));
      threads.add(thread);
    }
    threads.add(new Thread(new QueueWorker(bot), "Thread: " + QueueWorker.class.getSimpleName()));
    threads.forEach(Thread::start);
  }

}
