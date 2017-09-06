package ru.bigspawn.parser;

import static ru.bigspawn.parser.Main.logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.ini4j.Config;
import org.ini4j.Ini;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Configuration {

  private static final String SELECTION_URL = "URL";
  private static final String SECTION_BOT = "Bot";
  private static final String SECTION_PARSER = "Parser";
  private static final Configuration instance = new Configuration();

  private ArrayList<String> urls;
  private String telegramChanel;
  private String telegramBot;
  private String telegramBotName;
  private String dbUrl;
  private String dbUser;
  private String dbPassword;
  private String dbName;
  private String imagePath;
  private int sleepingTime;
  private int sleepingTimeForNews;
  private int maxRepeatedNews;

  private Configuration() {
    try {
      initConfigs();
    } catch (IOException e) {
      logger.error(e, e);
    }
  }

  private static void setIniConfigurations(Ini ini) {
    Config conf = new Config();
    conf.setMultiOption(true);
    ini.setConfig(conf);
  }

  public static Configuration getInstance() {
    return instance;
  }

  private void initConfigs() throws IOException {
    Ini ini = new Ini(new File("settings.ini"));
    setIniConfigurations(ini);
    Ini.Section section = ini.get(SELECTION_URL);
    String[] pagesStr = section.getAll("PAGE", String[].class);
    ArrayList<String> pages = new ArrayList<>(Arrays.asList(pagesStr));
    setUrls(pages);
    setTelegramBot(ini.get(SECTION_BOT, "TELEGRAM_BOT"));
    setTelegramBotName(ini.get(SECTION_BOT, "TELEGRAM_BOT_NAME"));
    setTelegramChanel(ini.get(SECTION_BOT, "TELEGRAM_CHANEL"));
    setDbUrl(ini.get(SECTION_PARSER, "DB_URL"));
    setDbUser(ini.get(SECTION_PARSER, "DB_USER"));
    setDbPassword(ini.get(SECTION_PARSER, "DB_PASSWD"));
    setDbName(ini.get(SECTION_PARSER, "DB_NAME"));
    setImagePath(ini.get(SECTION_PARSER, "IMAGES_PATH"));
    setSleepingTime(Integer.parseInt(ini.get(SECTION_PARSER, "SLEEPING_TIME")));
    setSleepingTimeForNews(
        Integer.parseInt(ini.get(SECTION_PARSER, "SLEEPING_TIME_FOR_NEWS")));
    setMaxRepeatedNews(Integer.parseInt(ini.get(SECTION_PARSER, "MAX_REPEATED_NEWS")));
    logger.info("Init configurations " + this);
  }

  public ArrayList<String> getUrls() {
    return urls;
  }

  public void setUrls(ArrayList<String> urls) {
    this.urls = urls;
  }

  public String getTelegramChanel() {
    return telegramChanel;
  }

  public void setTelegramChanel(String telegramChanel) {
    this.telegramChanel = telegramChanel;
  }

  public String getTelegramBot() {
    return telegramBot;
  }

  public void setTelegramBot(String telegramBot) {
    this.telegramBot = telegramBot;
  }

  public String getTelegramBotName() {
    return telegramBotName;
  }

  public void setTelegramBotName(String telegramBotName) {
    this.telegramBotName = telegramBotName;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  public String getDbUser() {
    return dbUser;
  }

  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public int getSleepingTime() {
    return sleepingTime;
  }

  public void setSleepingTime(int sleepingTime) {
    this.sleepingTime = sleepingTime;
  }

  public int getSleepingTimeForNews() {
    return sleepingTimeForNews;
  }

  public void setSleepingTimeForNews(int sleepingTimeForNews) {
    this.sleepingTimeForNews = sleepingTimeForNews;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public int getMaxRepeatedNews() {
    return maxRepeatedNews;
  }

  public void setMaxRepeatedNews(int maxRepeatedNews) {
    this.maxRepeatedNews = maxRepeatedNews;
  }

  @Override
  public String toString() {
    return "Configuration{" +
        "urls=" + urls +
        ", telegramChanel='" + telegramChanel + '\'' +
        ", telegramBot='" + telegramBot + '\'' +
        ", telegramBotName='" + telegramBotName + '\'' +
        ", dbUrl='" + dbUrl + '\'' +
        ", dbUser='" + dbUser + '\'' +
        ", dbPassword='" + dbPassword + '\'' +
        ", dbName='" + dbName + '\'' +
        ", imagePath='" + imagePath + '\'' +
        ", sleepingTime=" + sleepingTime +
        ", sleepingTimeForNews=" + sleepingTimeForNews +
        ", maxRepeatedNews=" + maxRepeatedNews +
        '}';
  }
}
