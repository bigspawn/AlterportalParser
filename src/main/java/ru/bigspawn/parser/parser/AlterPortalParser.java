package ru.bigspawn.parser.parser;

import static ru.bigspawn.parser.Constant.XPATH_NEWS_BODY;
import static ru.bigspawn.parser.Main.executor;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.bigspawn.parser.Utils;
import ru.bigspawn.parser.entity.News;
import ru.bigspawn.parser.parser.news.NewsPageParser;

public class AlterPortalParser implements Parser {

  private String pageUrl;
  private Logger logger;

  public AlterPortalParser(String pageUrl) {
    this.pageUrl = pageUrl;
    this.logger = LogManager.getLogger(Utils.getLoggerNameFromUrl(pageUrl));
  }

  @Override
  public List<News> parse(int pageNumber) throws UnsupportedEncodingException {
    String pageURL = getPageURL(pageNumber);
    logger.info("Start parsing news from " + pageURL);
    try (WebClient client = Utils.getWebClientWithoutCSSAndJS()) {
      HtmlPage page = client.getPage(pageURL);
      if (page.getBaseURL().getFile().contains("cgi-sys/suspendedpage.cgi")) {
        logger.error("Site is unavailable! Page: " + page.getBaseURL());
        return null;
      }
      return getNews(page);
    } catch (IOException e) {
      logger.error(e, e);
    }
    return null;
  }

  private String getPageURL(int pageNumber) throws UnsupportedEncodingException {
    return pageUrl + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + '/';
  }

  private List<News> getNews(HtmlPage page) {
    List<News> newsList = new ArrayList<>();
    List<Callable<News>> tasks = new ArrayList<>();
    List<HtmlElement> contents = page.getByXPath(XPATH_NEWS_BODY);
    contents.forEach(content -> tasks.add(new NewsPageParser(content, logger)));
    try {
      executor.invokeAll(tasks)
          .stream()
          .map(future -> {
            try {
              return future.get();
            } catch (Exception e) {
              throw new IllegalStateException(e);
            }
          })
          .forEach(newsList::add);
    } catch (InterruptedException e) {
      logger.error(e, e);
    }
//    logger.debug("List of news: " + Arrays.toString(newsList.toArray()));
    return newsList;
  }

}
