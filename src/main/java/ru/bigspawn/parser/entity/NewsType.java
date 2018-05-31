package ru.bigspawn.parser.entity;

import lombok.ToString;

/**
 * Created by bigspawn on 31.05.2017.
 */
@ToString
public enum NewsType {
  //  News("Новости"),
  Albums("Альбомы"),
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

}
