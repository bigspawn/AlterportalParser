package ru.bigspawn.parser;

import static ru.bigspawn.parser.Constant.XPATH_NEWS_BODY;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class Worker implements Runnable {

  private static final String SUSPENDED_SITE = "cgi-sys/suspendedpage.cgi";
  private static final Logger logger = LogManager.getLogger(Worker.class);

  private final NewsParser parser = new NewsPageParser();
  private final String pageUrl;
  private int pageNumber = 1;

  @Override
  public void run() {
    logger.info("Start worker: " + Thread.currentThread().getName());
    while (!Thread.currentThread().isInterrupted()) {
      try (WebClient client = Utils.getWebClientWithoutCSSAndJS()) {

        HtmlPage page = client.getPage(Utils.getPageURL(pageUrl, pageNumber));
        if (isUnavailable(page)) {
          logger.error(String.format("Site is unavailable! Page: %s", page.getBaseURL()));
          TimeUnit.MINUTES.sleep(Configuration.getInstance().getSleepingTime());
        } else {
          page.getByXPath(XPATH_NEWS_BODY).forEach(e -> parser.getNews((HtmlElement) e));
          pageNumber++;
        }

        if (pageNumber > 3) {
          pageNumber = 1;
          TimeUnit.MINUTES.sleep(Configuration.getInstance().getSleepingTime());
        }
      } catch (IOException | InterruptedException e) {
        logger.error(e, e);
      }
    }
  }

  private boolean isUnavailable(HtmlPage page) {
    return page.getBaseURL().getFile().contains(SUSPENDED_SITE);
  }


}
