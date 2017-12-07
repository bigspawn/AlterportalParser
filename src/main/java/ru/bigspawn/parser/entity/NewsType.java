package ru.bigspawn.parser.entity;

/**
 * Created by bigspawn on 31.05.2017.
 */
public enum NewsType {
  //  News("Новости"),
  Albums("Альбомы 2017"),
  Alternative("Alternative"),
  Punk("Punk"),
  Hardcore("Emo / Hardcore"),
  Metal("Metal"),
  Industrial("Industrial"),
  Rock("Rock"),
  Experimental("Experimental"),
  Lossless("Аудио CD"),
  NewTracks("Новые треки");

  private String name;

  NewsType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
