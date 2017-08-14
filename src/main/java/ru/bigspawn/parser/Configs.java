package ru.bigspawn.parser;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Configs {

  private static final Configs instance = new Configs();

  private String URL;
  private Integer LIST_CAPACITY;
  private String TELEGRAM_CHANEL;
  private String TELEGRAM_BOT;
  private String TELEGRAM_BOT_NAME;

  private Configs() {
  }

  public static Configs getInstance() {
    return instance;
  }

  public String getURL() {
    return URL;
  }

  public void setURL(String URL) {
    this.URL = URL;
  }

  public Integer getLIST_CAPACITY() {
    return LIST_CAPACITY;
  }

  public void setLIST_CAPACITY(Integer LIST_CAPACITY) {
    this.LIST_CAPACITY = LIST_CAPACITY;
  }

  public String getTELEGRAM_CHANEL() {
    return TELEGRAM_CHANEL;
  }

  public void setTELEGRAM_CHANEL(String TELEGRAM_CHANEL) {
    this.TELEGRAM_CHANEL = TELEGRAM_CHANEL;
  }

  public String getTELEGRAM_BOT() {
    return TELEGRAM_BOT;
  }

  public void setTELEGRAM_BOT(String TELEGRAM_BOT) {
    this.TELEGRAM_BOT = TELEGRAM_BOT;
  }

  public String getTELEGRAM_BOT_NAME() {
    return TELEGRAM_BOT_NAME;
  }

  public void setTELEGRAM_BOT_NAME(String TELEGRAM_BOT_NAME) {
    this.TELEGRAM_BOT_NAME = TELEGRAM_BOT_NAME;
  }
}
