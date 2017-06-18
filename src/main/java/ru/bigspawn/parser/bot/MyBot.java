package ru.bigspawn.parser.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.bigspawn.parser.Configs;
import ru.bigspawn.parser.News;

import static ru.bigspawn.parser.Main.logger;

/**
 * Created by bigspawn on 15.06.2017.
 */
public class MyBot extends TelegramLongPollingBot {

    public void sendNewsToChanel(News news, String chatId) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId);
        sendPhotoRequest.setPhoto(news.getImageURL());
        sendMessageToChannel(news, chatId, sendPhotoRequest);
    }

    private void sendMessageToChannel(News news, String chatId, SendPhoto sendPhotoRequest) {
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
