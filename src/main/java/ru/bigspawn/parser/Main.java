package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

  public static void main(String[] args) {
    try {
      logger.info("Start application");
      ApiContextInitializer.init();
      TelegramBotsApi botsApi = new TelegramBotsApi();
      Bot bot = new Bot();
      botsApi.registerBot(bot);
      startWorkers(bot);
    } catch (IOException | TelegramApiRequestException | InterruptedException e) {
      logger.error(e, e);
    }
  }

  private static void startWorkers(Bot bot)
      throws UnsupportedEncodingException, InterruptedException {
    List<String> urls = Configuration.getInstance().getUrls();
    logger.info("Start workers " + urls.size() + " - " + Arrays.toString(urls.toArray()));
    for (String url : urls) {
      startWorker(bot, url);
      TimeUnit.SECONDS.sleep(1);
    }
  }

  private static void startWorker(Bot bot, String url) throws UnsupportedEncodingException {
    Logger logger = LogManager.getLogger(url);
    Parser parser = new Parser(new WebClient(), url, logger);
    Worker worker = new Worker(parser, bot, logger);
    Thread thread = new Thread(worker, "Thread: " + url);
    thread.start();
  }
}
