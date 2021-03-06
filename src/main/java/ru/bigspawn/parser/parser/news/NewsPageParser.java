package ru.bigspawn.parser.parser.news;

import static ru.bigspawn.parser.Constant.XPATH_NEWS_BODY_DATE;
import static ru.bigspawn.parser.Constant.XPATH_NEWS_BODY_DIV;
import static ru.bigspawn.parser.Constant.XPATH_NEWS_BODY_TITLE;
import static ru.bigspawn.parser.Constant.XPATH_NEWS_CONTENT;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;
import ru.bigspawn.parser.Constant;
import ru.bigspawn.parser.Utils;
import ru.bigspawn.parser.entity.News;
import ru.bigspawn.parser.entity.NewsType;

public class NewsPageParser implements Callable<News>, NewsParser {

  private HtmlElement content;
  private Logger logger;

  public NewsPageParser(HtmlElement content, Logger logger) {
    this.content = content;
    this.logger = logger;
  }

  @Override
  public News call() {
    List<HtmlElement> categories = content.getElementsByAttribute("td", "class", "category");
    if (categories != null && !categories.isEmpty()) {
      String category = categories.get(0).asText().trim();
      Optional<NewsType> optional = Arrays.stream(NewsType.values())
          .filter(c -> category.equals(c.getName()) || category.contains(c.getName()))
          .findFirst();
      if (optional.isPresent()) {
        logger.debug("News category: " + category);
        NewsType type = optional.get();
        List<HtmlElement> titles = content.getElementsByAttribute("td", "class", "ntitle");
        if (titles != null && !titles.isEmpty()) {
          HtmlElement title = titles.get(0);
          String url = title.getElementsByTagName("a").get(0).getAttribute("href");
          logger.info("News url: " + url);
          News news = parse(type, url);
          if (news != null) {
            logger.debug("Parse news: " + news.getTitle());
            return news;
          }
        }
      }
    }
    return null;
  }

  @Override
  public News parse(NewsType type, String newsURL) {
    try (WebClient client = Utils.getWebClientWithoutCSSAndJS()) {
      HtmlPage page = client.getPage(newsURL);
      List<HtmlElement> contents = page.getByXPath(XPATH_NEWS_CONTENT);
      if (contents != null && !contents.isEmpty()) {
        HtmlElement content = contents.get(0);
        List<HtmlElement> bodies = content.getByXPath(XPATH_NEWS_BODY_DIV);
        if (bodies != null && !bodies.isEmpty()) {
          HtmlElement body = bodies.get(0);
          ArrayList<String> lines = getNewsLines(body);
          // FIXME: 21.10.2017 фигня когда возвращается одна строка
          News news = new News();
          news.setType(type);
          news.setTitle(getTitle(content));
          news.setGenre(getContentByTag(lines, "Стиль", "Жанр"));
          news.setFormat(getContentByTag(lines, "Формат", "Качество"));
          news.setCountry(getContentByTag(lines, "Страна"));
          news.setPlaylist(getTrackList(lines));
          news.setImageURL(getImageUrl(body));
          news.setDateTime(getDateTime(content));
          news.setPageURL(newsURL);
          news.setDownloadURL(getHref(body));
//          if (type != NewsType.News) {
//          }
          return news;
        }
      }
    } catch (IOException e) {
      logger.error(e, e);
    }
    return null;
  }

  private ArrayList<String> getNewsLines(HtmlElement body) {
    ArrayList<String> lines = new ArrayList<>(
        Arrays.asList(body.asText().split(System.lineSeparator())));
    lines.removeAll(Collections.singleton(Strings.EMPTY));
    return lines;
  }

  private String getContentByTag(ArrayList<String> lines, String... tags) {
    StringBuilder builder = new StringBuilder();
    for (String tag : tags) {
      if (builder.length() == 0) {
        findNewsTag(lines, builder, tag);
      } else {
        break;
      }
    }
    return replaceUnnecessarySymbols(builder.toString());
  }

  private void findNewsTag(ArrayList<String> lines, StringBuilder builder, String tag) {
    Optional<String> first = lines.stream()
        .filter(line -> line.toLowerCase().contains(tag.toLowerCase()))
        .findFirst();
    first.ifPresent(builder::append);
  }

  private String getTitle(HtmlElement element) {
    List<HtmlElement> titles = element.getByXPath(XPATH_NEWS_BODY_TITLE);
    if (titles != null && !titles.isEmpty()) {
      return titles.get(0).getTextContent();
    }
    return Strings.EMPTY;
  }

  private String replaceUnnecessarySymbols(String tag) {
    if (tag != null && !tag.isEmpty()) {
      return tag.replaceAll(":: ::", ":")
          .replaceAll("::", Strings.EMPTY)
          .replace(". Кач-во", Strings.EMPTY)
          .trim();
    }
    return Strings.EMPTY;
  }

  private DateTime getDateTime(HtmlElement content) {
    DateTime dateTime = DateTime.now();
    List<HtmlElement> dateElements = content.getByXPath(XPATH_NEWS_BODY_DATE);
    if (dateElements != null && !dateElements.isEmpty()) {
      String date = dateElements.get(0).getTextContent().trim();
      if (date.contains("Вчера")) {
        dateTime = dateTime.minusDays(1);
      } else if (!date.contains("Сегодня")) {
        Pattern pattern = Pattern.compile("\\|\\s.*");
        Matcher matcher = pattern.matcher(date);
        if (matcher.find()) {
          date = matcher.group();
          date = date.replace("|", Strings.EMPTY).trim();
          dateTime = Constant.FORMATTER.parseDateTime(date);
        }
      }
    }
    return dateTime;
  }

  private String getTrackList(ArrayList<String> lines) {
    StringBuilder tracks = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).contains("Треклист") || lines.get(i).contains("Tracklist")) {
        for (int j = i + 1; j < lines.size(); j++) {
          String track = lines.get(j).trim();
          if (Character.isDigit(track.charAt(0))) {
            tracks.append(track);
            if (j < lines.size() - 1 && Character.isDigit(lines.get(j + 1).charAt(0))) {
              tracks.append("\n");
            } else {
              break;
            }
          }
        }
        break;
      }
    }
    return tracks.toString();
  }

  private String getImageUrl(HtmlElement body) {
    List<HtmlElement> urls = body.getElementsByTagName("a");
    String imageUrl = getImageSrc(urls);
    if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.contains("fastpic")
        || !imageUrl.contains("radikal")) {
      imageUrl = getImageSrc(body.getElementsByTagName("img"));
    }
    return imageUrl;
  }

  private String getImageSrc(List<HtmlElement> imageElements) {
    if (imageElements != null && !imageElements.isEmpty()) {
      HtmlElement imageElement = imageElements.get(0);
      if (imageElement != null) {
        return imageElement.getAttribute("src");
      }
    }
    return Strings.EMPTY;
  }

  private String getHref(HtmlElement body) {
    List<HtmlElement> elements = body.getElementsByTagName("a");
    for (HtmlElement element : elements) {
      String href = element.getAttribute("href");
      for (String downloadResource : Constant.DOWNLOAD_RESOURCES) {
        if (href.contains(downloadResource)) {
          return href;
        }
      }
    }
    return Strings.EMPTY;
  }
}
