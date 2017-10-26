package ru.bigspawn.parser;

import static ru.bigspawn.parser.Constant.DOWNLOAD_BUTTON_TEXT;
import static ru.bigspawn.parser.Constant.NEWS_PAGE_BUTTON_TEXT;

import com.gargoylesoftware.htmlunit.WebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bigspawn.parser.entity.News;

public class Utils {

  public static String getLoggerNameFromUrl(String url) {
    Pattern pattern = Pattern.compile("/(\\w+)/");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group().replaceAll("/", "");
    }
    return url;
  }

  public static void appendIfNotNPE(StringBuilder builder, String... tags) {
    if (tags != null && tags.length > 0) {
      for (String tag : tags) {
        if (tag != null && !tag.isEmpty()) {
          builder.append(tag).append('\n');
        }
      }
    }
  }

  public static WebClient getWebClientWithoutCSSAndJS() {
    WebClient client = new WebClient();
    client.getOptions().setCssEnabled(false);
    client.getOptions().setJavaScriptEnabled(false);
    return client;
  }

  public static InlineKeyboardMarkup getInlineKeyboardMarkup(News news) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    addButtonIntoRow(news.getPageURL(), row, NEWS_PAGE_BUTTON_TEXT);
    addButtonIntoRow(news.getDownloadURL(), row, DOWNLOAD_BUTTON_TEXT);
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    return inlineKeyboardMarkup;
  }

  public static void addButtonIntoRow(String url, List<InlineKeyboardButton> row,
      String text) {
    if (url != null && !url.isEmpty()) {
      InlineKeyboardButton downloadButton = new InlineKeyboardButton();
      downloadButton.setText(text);
      downloadButton.setUrl(url);
      row.add(downloadButton);
    }
  }
}
