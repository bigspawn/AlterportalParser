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

  public static final Logger logger = LogManager.getLogger("main");

  public static void main(String[] args) {
    try {
      initConfigs();
      ApiContextInitializer.init();
      TelegramBotsApi botsApi = new TelegramBotsApi();
      Bot bot = new Bot();
      botsApi.registerBot(bot);
      List<String> urls = Configuration.getInstance().getUrls();
      for (String url : urls) {
        startWorker(bot, url);
        TimeUnit.SECONDS.sleep(30);
      }
    } catch (IOException | TelegramApiRequestException | InterruptedException e) {
      logger.error(e, e);
    }
  }

  private static void initConfigs() throws IOException {
    Ini ini = new Ini(new File("settings.ini"));
    setIniConfigurations(ini);
    Ini.Section section = ini.get("URL");
    String[] pagesStr = section.getAll("page", String[].class);
    ArrayList<String> pages = new ArrayList<>(Arrays.asList(pagesStr));
    Configuration.getInstance().setUrls(pages);
    Configuration.getInstance().setTelegramBot(ini.get("Bot", "TELEGRAM_BOT"));
    Configuration.getInstance().setTelegramBotName(ini.get("Bot", "TELEGRAM_BOT_NAME"));
    Configuration.getInstance().setTelegramChanel(ini.get("Bot", "TELEGRAM_CHANEL"));
    Configuration.getInstance().setDbUrl(ini.get("Parser", "DB_URL"));
    Configuration.getInstance().setDbUser(ini.get("Parser", "DB_User"));
    Configuration.getInstance().setDbPasswd(ini.get("Parser", "DB_Passwd"));
    Configuration.getInstance().setDbName(ini.get("Parser", "DB_Name"));
    Configuration.getInstance()
        .setSleepingTime(Integer.parseInt(ini.get("Parser", "Sleeping_Time")));
    Configuration.getInstance()
        .setSleepingTimeForNews(Integer.parseInt(ini.get("Parser", "Sleeping_Time_For_News")));
  }

  private static void setIniConfigurations(Ini ini) {
    Config conf = new Config();
    conf.setMultiOption(true);
    ini.setConfig(conf);
  }

  private static void startWorker(Bot bot, String url) throws UnsupportedEncodingException {
    Logger logger = LogManager.getLogger(url);
    Parser parser = new Parser(new WebClient(), url, logger);
    Worker worker = new Worker(parser, bot, logger);
    Thread thread = new Thread(worker, "Thread: " + url);
    thread.start();
  }
}
