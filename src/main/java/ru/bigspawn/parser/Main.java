package ru.bigspawn.parser;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.bigspawn.parser.bot.Bot;
import ru.bigspawn.parser.parser.AlterPortalParser;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {

  public static final Logger logger = LogManager.getLogger(Main.class.getName());
  public static final ExecutorService executor = Executors
      .newFixedThreadPool(Configuration.getInstance().getThreads());

  public static void main(String[] args) {
    try {
      logger.info("Start application");
      logger.info(System.getProperties().get("log4j.configurationFile"));
      Class.forName("org.postgresql.Driver");
      ApiContextInitializer.init();
      Bot bot = getTelegramBot();
      startWorkers(bot);
    } catch (ClassNotFoundException | TelegramApiRequestException e) {
      logger.error(e, e);
    }
  }

  private static Bot getTelegramBot() throws TelegramApiRequestException {
    DefaultBotOptions instance = ApiContext.getInstance(DefaultBotOptions.class);
    if (Configuration.getInstance().isUseProxy()) {
      RequestConfig requestConfig = RequestConfig.copy(RequestConfig.custom().build())
          .setProxy(
              new HttpHost(
                  Configuration.getInstance().getProxyHost(),
                  Configuration.getInstance().getProxyPort()))
          .build();
      instance.setRequestConfig(requestConfig);
    }
    Bot bot = new Bot(instance);
    TelegramBotsApi botsApi = new TelegramBotsApi();
    botsApi.registerBot(bot);
    return bot;
  }

  private static void startWorkers(Bot bot) {
    List<String> urls = Configuration.getInstance().getUrls();
    logger
        .info(String.format("Start workers %d - %s", urls.size(), Arrays.toString(urls.toArray())));
    for (String url : urls) {
      startWorker(bot, url);
    }
  }

  private static void startWorker(Bot bot, String url) {
    AlterPortalParser parser = new AlterPortalParser(url);
    String loggerName = Utils.getLoggerNameFromUrl(url);
    Worker worker = new Worker(parser, bot, loggerName);
    logger.debug("Create " + worker);
    Thread thread = new Thread(worker, "Thread: " + loggerName);
    thread.start();
    logger.debug("Start " + thread);
  }
}
