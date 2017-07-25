package ru.bigspawn.parser.bot;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configs;
import ru.bigspawn.parser.News;

import java.io.IOException;
import java.util.List;

import static ru.bigspawn.parser.Main.logger;

/**
 * Created by bigspawn on 15.06.2017.
 */
public class MyBot extends TelegramLongPollingBot {

  private WebClient client = new WebClient();
  private HtmlPage page;
  private int counter;

  public void sendNewsToChanel(News news, String chatId) throws Exception {
    SendPhoto sendPhotoRequest = new SendPhoto();
    sendPhotoRequest.setChatId(chatId);
    sendPhotoRequest.setPhoto(news.getImageURL());
    sendMessageToChannel(news, chatId, sendPhotoRequest);
  }

  private void sendMessageToChannel(News news, String chatId, SendPhoto sendPhotoRequest)
      throws Exception {
    try {
      sendPhoto(sendPhotoRequest);
      sendMessage(new SendMessage(chatId, news.getTextForMessage()));
      logger.info("Send new news: " + news.getTitle() + "to channel");
    } catch (TelegramApiException e) {
      logger.error(e, e);
    }
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      SendMessage message = new SendMessage()
          .setChatId(update.getMessage().getChatId())
          .setText(update.getMessage().getText());
      logger.info("Bot get message: " + message);
    }
  }

  @Override
  public String getBotUsername() {
    return Configs.getInstance().getTELEGRAM_BOT_NAME();
  }

  @Override
  public String getBotToken() {
    return Configs.getInstance().getTELEGRAM_BOT();
  }
}
