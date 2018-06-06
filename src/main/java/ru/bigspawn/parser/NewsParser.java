package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import ru.bigspawn.parser.entity.News;
import ru.bigspawn.parser.entity.NewsType;

public interface NewsParser {

  void getNews(HtmlElement content);

  News parse(NewsType type, String newsURL);

}
