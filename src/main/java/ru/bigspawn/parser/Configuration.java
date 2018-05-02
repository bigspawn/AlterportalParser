package ru.bigspawn.parser;

import static ru.bigspawn.parser.Main.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  private int sleepingTime;
  private int sleepingTimeForNews;
  private int maxRepeatedNews;
  private int threads;
  private boolean useProxy;
  private String proxyHost;
  private int proxyPort;

  private Configuration() {
    try {
      initConfigs();
    } catch (IOException e) {
      logger.error(e, e);
    }
  }

  public static Configuration getInstance() {
    return instance;
  }

  private static void setIniConfigurations(Ini ini) {
    Config conf = new Config();
    conf.setMultiOption(true);
    ini.setConfig(conf);
  }

  private void initConfigs() throws IOException {
    Path path = Paths.get(System.getProperty("settings.path"));
    File input = new File(path.toUri());
    Ini ini = new Ini(input);
    setIniConfigurations(ini);
    Ini.Section section = ini.get(SELECTION_URL);
    String[] pagesStr = section.getAll("PAGE", String[].class);
    ArrayList<String> pages = new ArrayList<>(Arrays.asList(pagesStr));
    setUrls(pages);
    setTelegramBot(ini.get(SECTION_BOT, "TELEGRAM_BOT"));
    setTelegramBotName(ini.get(SECTION_BOT, "TELEGRAM_BOT_NAME"));
    setTelegramChanel(ini.get(SECTION_BOT, "TELEGRAM_CHANEL"));
    setUseProxy(ini.get(SECTION_BOT, "PROXY_HOST") != null);
    if (useProxy) {
      setProxyHost(ini.get(SECTION_BOT, "PROXY_HOST"));
      setProxyPort(Integer.valueOf(ini.get(SECTION_BOT, "PROXY_PORT")));
    }
    setDbUrl(ini.get(SECTION_PARSER, "DB_URL"));
    setDbUser(ini.get(SECTION_PARSER, "DB_USER"));
    setDbPassword(ini.get(SECTION_PARSER, "DB_PASSWD"));
    setDbName(ini.get(SECTION_PARSER, "DB_NAME"));
    setSleepingTime(Integer.valueOf(ini.get(SECTION_PARSER, "SLEEPING_TIME")));
    setSleepingTimeForNews(Integer.valueOf(ini.get(SECTION_PARSER, "SLEEPING_TIME_FOR_NEWS")));
    setMaxRepeatedNews(Integer.valueOf(ini.get(SECTION_PARSER, "MAX_REPEATED_NEWS")));
    setThreads(Integer.valueOf(ini.get(SECTION_PARSER, "THREADS_COUNT")));
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

  public int getMaxRepeatedNews() {
    return maxRepeatedNews;
  }

  public void setMaxRepeatedNews(int maxRepeatedNews) {
    this.maxRepeatedNews = maxRepeatedNews;
  }

  public int getThreads() {
    return threads;
  }

  public void setThreads(int threads) {
    this.threads = threads;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public boolean isUseProxy() {
    return useProxy;
  }

  public void setUseProxy(boolean useProxy) {
    this.useProxy = useProxy;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("Configuration{").append("urls=").append(urls)
        .append(", telegramChanel='").append(telegramChanel).append('\'').append(", telegramBot='")
        .append(telegramBot).append('\'').append(", telegramBotName='").append(telegramBotName)
        .append('\'').append(", dbUrl='").append(dbUrl).append('\'').append(", dbUser='")
        .append(dbUser).append('\'').append(", dbPassword='").append(dbPassword).append('\'')
        .append(", dbName='").append(dbName).append('\'').append(", sleepingTime=")
        .append(sleepingTime).append(", sleepingTimeForNews=").append(sleepingTimeForNews)
        .append(", maxRepeatedNews=").append(maxRepeatedNews).append(", threads=").append(threads)
        .append(", useProxy=").append(useProxy).append(", proxyHost='").append(proxyHost)
        .append('\'').append(", proxyPort=").append(proxyPort).append('}').toString();
  }
}
