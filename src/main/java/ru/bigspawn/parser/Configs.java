package ru.bigspawn.parser;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Configs {

  private static final Configs instance = new Configs();

  private String url;
  private String telegramChanel;
  private String telegramBot;
  private String telegramBotName;
  private String dbUrl;
  private String dbUser;
  private String dbPasswd;
  private String dbName;

  private Configs() {
  }

  public static Configs getInstance() {
    return instance;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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
}
