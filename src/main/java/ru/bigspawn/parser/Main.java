package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.bigspawn.parser.bot.Bot;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {

  public static final Logger logger = LogManager.getLogger(Main.class.getName());
  private static final String SELECTION_URL = "URL";
  private static final String SECTION_BOT = "Bot";
  private static final String SECTION_PARSER = "Parser";
  private static Configuration config = Configuration.getInstance();

  public static void main(String[] args) {
    try {
      logger.info("Start application");
      initConfigs();
      ApiContextInitializer.init();
      TelegramBotsApi botsApi = new TelegramBotsApi();
      Bot bot = new Bot();
      botsApi.registerBot(bot);
      startWorkers(bot);
    } catch (IOException | TelegramApiRequestException | InterruptedException e) {
      logger.error(e, e);
    }
  }

  private static void initConfigs() throws IOException {
    Ini ini = new Ini(new File("settings.ini"));
    setIniConfigurations(ini);
    Ini.Section section = ini.get(SELECTION_URL);
    String[] pagesStr = section.getAll("PAGE", String[].class);
    ArrayList<String> pages = new ArrayList<>(Arrays.asList(pagesStr));
    config.setUrls(pages);
    config.setTelegramBot(ini.get(SECTION_BOT, "TELEGRAM_BOT"));
    config.setTelegramBotName(ini.get(SECTION_BOT, "TELEGRAM_BOT_NAME"));
    config.setTelegramChanel(ini.get(SECTION_BOT, "TELEGRAM_CHANEL"));
    config.setDbUrl(ini.get(SECTION_PARSER, "DB_URL"));
    config.setDbUser(ini.get(SECTION_PARSER, "DB_USER"));
    config.setDbPasswd(ini.get(SECTION_PARSER, "DB_PASSWD"));
    config.setDbName(ini.get(SECTION_PARSER, "DB_NAME"));
    config.setImagePath(ini.get(SECTION_PARSER, "IMAGES_PATH"));
    config.setSleepingTime(Integer.parseInt(ini.get(SECTION_PARSER, "SLEEPING_TIME")));
    config.setSleepingTimeForNews(
        Integer.parseInt(ini.get(SECTION_PARSER, "SLEEPING_TIME_FOR_NEWS")));
    config.setMaxRepeatedNews(Integer.parseInt(ini.get(SECTION_PARSER, "MAX_REPEATED_NEWS")));
    logger.info("Init configurations " + config);
  }

  private static void setIniConfigurations(Ini ini) {
    Config conf = new Config();
    conf.setMultiOption(true);
    ini.setConfig(conf);
  }

  private static void startWorkers(Bot bot)
      throws UnsupportedEncodingException, InterruptedException {
    List<String> urls = config.getUrls();
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
