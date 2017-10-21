package ru.bigspawn.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constant {

  public static final String XPATH_NEWS_CONTENT = "//div[@id=\"dle-content\"]";
  public static final String XPATH_NEWS_BODY =
      XPATH_NEWS_CONTENT + "/table/tbody/tr[1]/td[@class=\"abl_12\"]/table";
  public static final String XPATH_NEWS_BODY_TITLE = "//table/tbody/tr/td/table/tbody/tr/td[@class=\"ntitle\"]";
  public static final String XPATH_NEWS_BODY_DATE = "//*[@id=\"dle-content\"]/table/tbody/tr/td/div[@class=\"slink1\"]";
  public static final String XPATH_NEWS_BODY_DIV = "//div[contains(@id, \"news-id\")]";
  public static final String DOWNLOAD_BUTTON_TEXT = "Download";
  public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("dd MMMM yyyy")
      .withLocale(new Locale("ru"));
  public static final List<String> DOWNLOAD_RESOURCES = Collections
      .unmodifiableList(new ArrayList<String>() {
        {
          add("mail.ru");
          add("yadi.sk");
          add("rgho.st");
          add("mystore.to");
          add("mediafire.com");
          add("zippyshare.com");
          add("mega.nz");
          add("solidfiles.com");
          add("adrive.com");
          add("firedrive.com");
          add("my-files.ru");
        }
      });
}
