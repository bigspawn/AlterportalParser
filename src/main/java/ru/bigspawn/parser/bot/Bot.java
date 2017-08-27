package ru.bigspawn.parser.bot;

import static ru.bigspawn.parser.Main.logger;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configuration;
import ru.bigspawn.parser.News;

/**
 * Created by bigspawn on 15.06.2017.
 */
public class Bot extends TelegramLongPollingBot {

  public synchronized void sendNewsToChanel(News news, String chatId) throws Exception {
    if (news.getImageURL() != null) {
      sendPhotoIntoChannel(news, chatId);
    }
    String textForMessage = news.getTextForMessage();
    if (textForMessage != null && !textForMessage.isEmpty()) {
      sendMessageToChannel(chatId, news);
    }
  }

  private void sendPhotoIntoChannel(News news, String chatId) {
    String imageURL = news.getImageURL();
    SendPhoto sendPhotoRequest = new SendPhoto();
    sendPhotoRequest.setChatId(chatId);
    sendPhotoRequest.setPhoto(imageURL);
    try {
      sendPhoto(sendPhotoRequest);
    } catch (TelegramApiException e) {
      logger.error(e, e);
      //todo как то вытаскивать картинки
    }
  }

  private void sendMessageToChannel(String chatId, News news) {
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
      logger.info("Bot get message: " + message);
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
    button.setText("Download");
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
