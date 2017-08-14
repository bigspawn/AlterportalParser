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
import ru.bigspawn.parser.bot.MyBot;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {

  public static final Logger logger = LogManager.getLogger("RollingFile");

  public static void main(String[] args) {
    try {
      Wini wini = new Wini(new File("settings.ini"));
      Configs.getInstance().setURL(wini.get("Parser", "URL"));
      Configs.getInstance().setLIST_CAPACITY(Integer.valueOf(wini.get("Parser", "LIST_CAPACITY")));
      Configs.getInstance().setTELEGRAM_BOT(wini.get("Bot", "TELEGRAM_BOT"));
      Configs.getInstance().setTELEGRAM_BOT_NAME(wini.get("Bot", "TELEGRAM_BOT_NAME"));
      Configs.getInstance().setTELEGRAM_CHANEL(wini.get("Bot", "TELEGRAM_CHANEL"));
      ApiContextInitializer.init();
      TelegramBotsApi botsApi = new TelegramBotsApi();
      MyBot bot = new MyBot();
      botsApi.registerBot(bot);
      startWorker(bot);
    } catch (UnsupportedEncodingException e) {
      logger.error(e, e);
      return;
    } catch (IOException | TelegramApiRequestException e) {
      logger.error(e, e);
    }
  }

  private static void startWorker(MyBot bot) throws UnsupportedEncodingException {
    Worker worker = new Worker(bot);
    Thread thread = new Thread(worker);
    thread.start();
  }
}
