package ru.bigspawn.parser;

import java.util.ArrayList;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Configuration {

  private static final Configuration instance = new Configuration();

  private ArrayList<String> urls;
  private String telegramChanel;
  private String telegramBot;
  private String telegramBotName;
  private String dbUrl;
  private String dbUser;
  private String dbPasswd;
  private String dbName;
  private String imagePath;
  private int sleepingTime;
  private int sleepingTimeForNews;
  private int maxRepeatedNews;

  private Configuration() {
  }

  public static Configuration getInstance() {
    return instance;
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

  public String getDbPasswd() {
    return dbPasswd;
  }

  public void setDbPasswd(String dbPasswd) {
    this.dbPasswd = dbPasswd;
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
}
