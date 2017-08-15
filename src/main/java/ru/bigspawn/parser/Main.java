package ru.bigspawn.parser;

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
      startWorker(bot);
    } catch (IOException | TelegramApiRequestException e) {
      logger.error(e, e);
    }
  }

  private static void initConfigs() throws IOException {
    Wini wini = new Wini(new File("settings.ini"));
    Configs.getInstance().setUrl(wini.get("Parser", "URL"));
    Configs.getInstance().setListCapacity(Integer.valueOf(wini.get("Parser", "LIST_CAPACITY")));
    Configs.getInstance().setTelegramBot(wini.get("Bot", "TELEGRAM_BOT"));
    Configs.getInstance().setTelegramBotName(wini.get("Bot", "TELEGRAM_BOT_NAME"));
    Configs.getInstance().setTelegramChanel(wini.get("Bot", "TELEGRAM_CHANEL"));
    Configs.getInstance().setDbUrl(wini.get("Parser", "DB_URL"));
    Configs.getInstance().setDbUser(wini.get("Parser", "DB_User"));
    Configs.getInstance().setDbPasswd(wini.get("Parser", "DB_Passwd"));
  }

  private static void startWorker(Bot bot) throws UnsupportedEncodingException {
    Worker worker = new Worker(bot);
    Thread thread = new Thread(worker);
    thread.start();
  }
}
