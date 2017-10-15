package ru.bigspawn.parser.parser;

import ru.bigspawn.parser.entity.News;
import ru.bigspawn.parser.entity.NewsType;

public interface NewsParser {

  News parse(NewsType type, String newsURL);

}
