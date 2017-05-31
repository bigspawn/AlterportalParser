package ru.bigspawn.parser;

/**
 * Created by bigspawn on 31.05.2017.
 */
public enum NewsType {
    News("Новости"),
    Albums("Альбомы 2017"),
    Alternative("Alternative"),
    Punk("Punk"),
    Hardcore("Emo / Hardcore"),
    Metal("Metal"),
    Industrial("Industrial"),
    Rock("Rock"),
    Experimental("Experimental"),
    Lossless("Аудио CD (lossless)"),
    NewTracks("Новые треки"),
    Concerts("Концерты");

    private String name;

    NewsType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
