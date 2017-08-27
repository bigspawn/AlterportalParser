package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Wini;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.bigspawn.parser.bot.Bot;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {

  public static final Logger logger = LogManager.getLogger("RollingFile");

  public static void main(String[] args) {
    try {
      initConfigs();
      ApiContextInitializer.init();
      TelegramBotsApi botsApi = new TelegramBotsApi();
      Bot bot = new Bot();
      botsApi.registerBot(bot);
      startWorker(bot, Configuration.getInstance().getUrl());
    } catch (IOException | TelegramApiRequestException e) {
      logger.error(e, e);
    }
  }

  private static void initConfigs() throws IOException {
    Wini wini = new Wini(new File("settings.ini"));
    Configuration.getInstance().setUrl(wini.get("Parser", "URL"));
    Configuration.getInstance().setTelegramBot(wini.get("Bot", "TELEGRAM_BOT"));
    Configuration.getInstance().setTelegramBotName(wini.get("Bot", "TELEGRAM_BOT_NAME"));
    Configuration.getInstance().setTelegramChanel(wini.get("Bot", "TELEGRAM_CHANEL"));
    Configuration.getInstance().setDbUrl(wini.get("Parser", "DB_URL"));
    Configuration.getInstance().setDbUser(wini.get("Parser", "DB_User"));
    Configuration.getInstance().setDbPasswd(wini.get("Parser", "DB_Passwd"));
    Configuration.getInstance().setDbName(wini.get("Parser", "DB_Name"));
    Configuration.getInstance()
        .setSleepingTime(Integer.parseInt(wini.get("Parser", "Sleeping_Time")));
    Configuration.getInstance()
        .setSleepingTimeForNews(Integer.parseInt(wini.get("Parser", "Sleeping_Time_For_News")));
  }

  private static void startWorker(Bot bot, String url) throws UnsupportedEncodingException {
    Parser parser = new Parser(new WebClient(), url);
    Worker worker = new Worker(parser, bot);
    Thread thread = new Thread(worker);
    thread.start();
  }
}
