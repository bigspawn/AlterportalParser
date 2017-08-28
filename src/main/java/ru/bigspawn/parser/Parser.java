package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Parser {

  static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");

  private WebClient client;
  private String pageUrl;
  private Logger logger;

  public Parser(WebClient client, String pageUrl, Logger logger)
      throws UnsupportedEncodingException {
    this.client = client;
    this.pageUrl = pageUrl;
    this.logger = logger;
    setOptions();
  }

  private void setOptions() {
    client.getOptions().setCssEnabled(false);
    client.getOptions().setJavaScriptEnabled(false);
  }

  public List<News> parse(int pageNumber) throws IOException {
    String pageURL = getPageURL(pageNumber);
    logger.info("Start parsing news from " + pageURL);
    List<News> newsList = new ArrayList<>();
    HtmlPage page = client.getPage(pageURL);
    List<HtmlElement> elements = page
        .getByXPath("//*[@id=\"dle-content\"]/table/tbody/tr/td/table");
    for (HtmlElement element : elements) {
      List<HtmlElement> titleTds = element.getElementsByAttribute("td", "class", "category");
      if (titleTds != null && !titleTds.isEmpty()) {
        String newsCategory = titleTds.get(0).asText().trim();
        Optional<NewsType> optional = Arrays.stream(NewsType.values())
            .filter(x -> newsCategory.equals(x.getName()))
            .findFirst();
        if (optional.isPresent()) {
          NewsType type = optional.get();
          HtmlElement titleElement = element
              .getElementsByAttribute("td", "class", "ntitle")
              .get(0);
          String newsURL = titleElement.getElementsByTagName("a").get(0).getAttribute("href");
          page = client.getPage(newsURL);
          List<HtmlElement> newsElements = page.getByXPath("//*[@id=\"dle-content\"]");
          if (newsElements != null && !newsElements.isEmpty()) {
            HtmlElement newsElement = newsElements.get(0);
            News news = new News();
            news.setType(type);
            news.setTitle(getNewsTitle(newsElement));

            HtmlElement newsBodyHtmlElement = (HtmlElement) newsElement
                .getByXPath("//div[contains(@id, \"news-id\")]").get(0);
            ArrayList<String> lines = getNewsAsStringArray(newsBodyHtmlElement);
            news.setGenre(getNewsTag(lines, "Стиль", "Жанр"));
            if (type != NewsType.Concerts) {
              news.setCountry(getNewsTag(lines, "Страна", "Родина"));
              news.setFormat(getNewsTag(lines, "Формат", "Качество"));
            }
            news.setPlaylist(getTrackList(lines));
            List<HtmlElement> aElements = newsBodyHtmlElement.getElementsByTagName("a");
            if (aElements != null && !aElements.isEmpty()) {
              String imageUrl = getImageUrl(aElements);
              if (imageUrl == null || imageUrl.isEmpty()
                  || !imageUrl.contains("fastpic")
                  || !imageUrl.contains("radikal")) {
                imageUrl = getImageSrc(newsBodyHtmlElement.getElementsByTagName("img"));
              }
              news.setImageURL(imageUrl);
              if (type != NewsType.Concerts && type != NewsType.News) {
                news.setDownloadURL(getHref(aElements));
              }
            }
            news.setDateTime(getDateTime(newsElement));
            newsList.add(news);
          }
        }
      }
    }
    logger
        .info("All parsed news (" + newsList.size() + "): `" + Arrays.toString(newsList.toArray()));
    logger.info("Finish parsing.");
    return newsList;
  }

  private String getPageURL(int pageNumber) throws UnsupportedEncodingException {
    return pageUrl + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + "/";
  }

  private ArrayList<String> getNewsAsStringArray(HtmlElement newsBodyHtmlElement) {
    String newsBody = newsBodyHtmlElement.asText();
    ArrayList<String> lines = new ArrayList<>(Arrays.asList(newsBody.split("\r\n")));
    lines.removeAll(Collections.singleton(""));
    return lines;
  }

  private String getNewsTag(ArrayList<String> lines, String firstTag, String secondTag) {
    StringBuilder formatBuilder = new StringBuilder();
    findNewsTag(lines, formatBuilder, firstTag);
    if (formatBuilder.length() == 0) {
      findNewsTag(lines, formatBuilder, secondTag);
    }
    return replaceUnnecessarySymbols(formatBuilder.toString());
  }

  private void findNewsTag(ArrayList<String> lines, StringBuilder builder, String tag) {
    Optional<String> first = lines.stream()
        .filter(line -> StringUtils.containsIgnoreCase(line, tag))
        .findFirst();
    first.ifPresent(builder::append);
  }

  private String getNewsTitle(HtmlElement newsElement) {
    return ((HtmlElement) newsElement
        .getByXPath("//table/tbody/tr/td/table/tbody/tr/td[@class=\"ntitle\"]").get(0))
        .getTextContent();
  }

  private String replaceUnnecessarySymbols(String str) {
    return str.replaceAll(":: ::", ":")
        .replaceAll("::", "")
        .replace(". Кач-во", "")
        .trim();
  }

  private DateTime getDateTime(HtmlElement newsElement) {
    DateTime dateTime = DateTime.now();
    List<HtmlElement> commentElements = newsElement.getByXPath(
        "//*[@id=\"dle-content\"]/table/tbody/tr/td/div[@class=\"slink1\"]");
    if (commentElements != null && !commentElements.isEmpty()) {
      String date = commentElements.get(0).getTextContent().trim();
      if (StringUtils.contains(date, "Вчера")) {
        dateTime = dateTime.minusDays(1);
      } else if (!StringUtils.contains(date, "Сегодня")) {
        Pattern pattern = Pattern.compile("\\|\\s.*");
        Matcher matcher = pattern.matcher(date);
        if (matcher.find()) {
          date = matcher.group();
          date = date.replace("|", "").trim();
          dateTime = formatter.parseDateTime(date);
        }
      }
    }
    return dateTime;
  }

  private String getTrackList(ArrayList<String> lines) {
    StringBuilder tracks = new StringBuilder("");
    for (int i = 0; i < lines.size(); i++) {
      if (StringUtils.contains(lines.get(i), "Треклист")
          || StringUtils.contains(lines.get(i), "Tracklist")) {
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

  private String getImageUrl(List<HtmlElement> aElements) {
    List<HtmlElement> imageElements = aElements.get(0).getElementsByTagName("img");
    return getImageSrc(imageElements);
  }

  private String getImageSrc(List<HtmlElement> imageElements) {
    if (imageElements != null && !imageElements.isEmpty()) {
      HtmlElement imageElement = imageElements.get(0);
      if (imageElement != null) {
        return imageElement.getAttribute("src");
      }
    }
    return "";

  }

  private String getHref(List<HtmlElement> elements) {
    for (HtmlElement element : elements) {
      String href = element.getAttribute("href");
      for (String downloadResource : Constant.DOWNLOAD_RESOURCES) {
        if (href.contains(downloadResource)) {
          return href;
        }
      }
    }
    return "";
  }
}
