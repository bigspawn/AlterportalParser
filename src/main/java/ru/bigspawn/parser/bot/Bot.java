package ru.bigspawn.parser.bot;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configuration;
import ru.bigspawn.parser.Main;
import ru.bigspawn.parser.Utils;
import ru.bigspawn.parser.entity.News;


/**
 * Created by bigspawn on 15.06.2017.
 */
public class Bot extends TelegramLongPollingBot {

  public synchronized void sendNewsToChannel(News news, String chatId, Logger logger) {
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
      sendDownloadedImage(news, chatId, logger);
    }
  }

  private void sendDownloadedImage(News news, String chatId, Logger logger) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(news.getImageURL()).build();
    Response response;
    try {
      response = client.newCall(request).execute();
      logger.info("Get response: " + response);
      if (!response.isSuccessful()) {
        throw new IOException("Failed to download file: " + response);
      }
      if (response.body() != null) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId);
        sendPhotoRequest.setNewPhoto(news.getTitle(), response.body().byteStream());
        sendPhoto(sendPhotoRequest);
      }
    } catch (IOException | TelegramApiException e) {
      logger.error(e, e);
    }
  }

  private void sendMessageToChannel(String chatId, News news, Logger logger) {
    try {
      if (news.getDownloadURL() != null && !news.getDownloadURL().isEmpty()) {
        sendMessage(sendNewsWithDownloadButton(chatId, news));
      } else {
//        sendMessage(new SendMessage(chatId, news.getTextForMessage()));
        logger.error("News with empty download url! " + news);
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
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(news.getTextForMessage());

    InlineKeyboardMarkup inlineKeyboardMarkup = Utils.getInlineKeyboardMarkup(news);
    if (!inlineKeyboardMarkup.getKeyboard().isEmpty()) {
      message.setReplyMarkup(inlineKeyboardMarkup);
    }
    return message;
  }
}
