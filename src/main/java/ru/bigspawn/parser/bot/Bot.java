package ru.bigspawn.parser.bot;

import static ru.bigspawn.parser.Constant.DOWNLOAD_BUTTON_TEXT;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configuration;
import ru.bigspawn.parser.Main;
import ru.bigspawn.parser.entity.News;


/**
 * Created by bigspawn on 15.06.2017.
 */
public class Bot extends TelegramLongPollingBot {

  public synchronized void sendNewsToChannel(News news, String chatId, Logger logger)
      throws Exception {
    logger.debug("Bot send news " + news + " into channel");
    if (news.getImageURL() != null) {
      sendPhotoIntoChannel(news, chatId, logger);
    }
    String textForMessage = news.getTextForMessage();
    if (textForMessage != null && !textForMessage.isEmpty()) {
      sendMessageToChannel(chatId, news, logger);
    }
  }

  private void sendPhotoIntoChannel(News news, String chatId, Logger logger) {
    String imageURL = news.getImageURL();
    SendPhoto sendPhotoRequest = new SendPhoto();
    sendPhotoRequest.setChatId(chatId);
    sendPhotoRequest.setPhoto(imageURL);
    try {
      logger.debug("Send image: " + imageURL);
      sendPhoto(sendPhotoRequest);
    } catch (TelegramApiException e) {
      logger.error(e, e);
      //todo как то вытаскивать картинки
    }
  }

  private void sendMessageToChannel(String chatId, News news, Logger logger) {
    try {
      if (news.getDownloadURL() != null && !news.getDownloadURL().isEmpty()) {
        sendMessage(sendNewsWithDownloadButton(chatId, news));
      } else {
        sendMessage(new SendMessage(chatId, news.getTextForMessage()));
      }
      logger.info("Send news: " + news.getTitle() + " to channel");
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
      Main.logger.info("Bot get message: " + message);
    }
  }

  @Override
  public String getBotUsername() {
    return Configuration.getInstance().getTelegramBotName();
  }

  @Override
  public String getBotToken() {
    return Configuration.getInstance().getTelegramBot();
  }

  private SendMessage sendNewsWithDownloadButton(String chatId, News news) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setText(DOWNLOAD_BUTTON_TEXT);
    button.setUrl(news.getDownloadURL());
    row.add(button);
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(news.getTextForMessage());
    message.setReplyMarkup(inlineKeyboardMarkup);
    return message;
  }
}
