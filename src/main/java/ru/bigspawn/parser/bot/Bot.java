package ru.bigspawn.parser.bot;

import static ru.bigspawn.parser.Main.logger;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configs;
import ru.bigspawn.parser.News;

/**
 * Created by bigspawn on 15.06.2017.
 */
public class Bot extends TelegramLongPollingBot {

  public void sendNewsToChanel(News news, String chatId) throws Exception {
    if (news.getImageURL() != null) {
      sendPhotoIntoChannel(news, chatId);
    }
    String textForMessage = news.getTextForMessage();
    if (textForMessage != null && !textForMessage.isEmpty()) {
      sendMessageToChannel(news, chatId);
    }
  }

  private void sendPhotoIntoChannel(News news, String chatId) {
    SendPhoto sendPhotoRequest = new SendPhoto();
    sendPhotoRequest.setChatId(chatId);
    sendPhotoRequest.setPhoto(news.getImageURL());
    try {
      sendPhoto(sendPhotoRequest);
    } catch (TelegramApiException e) {
      logger.error(e, e);
    }
  }

  private void sendMessageToChannel(News news, String chatId) {
    SendMessage message = new SendMessage(chatId, news.getTextForMessage());
    try {
      sendMessage(message);
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
    return Configs.getInstance().getTelegramBotName();
  }

  @Override
  public String getBotToken() {
    return Configs.getInstance().getTelegramBot();
  }
}
