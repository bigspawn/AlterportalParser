package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {
    private static ArrayList<News> newsArrayList = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO: 31.05.2017 добавлять все в базу
        // TODO: 31.05.2017 обрабатывать логику повторяющихся статей
//        try {
//            Class.forName("org.h2.Driver");
//            Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "admin", "52169248");
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        TimeUnit.SECONDS.sleep(20);
        int pageNumber = 1;

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        try {
            String url = "http://alterportal.ru/page/" + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + "/";
            HtmlPage page = client.getPage(url);
            List<HtmlElement> elements = page.getByXPath("//*[@id=\"dle-content\"]/table/tbody/tr/td/table");
            System.out.println("Get " + elements.size() + " newsArrayList from page № " + pageNumber);
            for (HtmlElement element : elements) {
                List<HtmlElement> titleTds = element.getElementsByAttribute("td", "class", "category");
                if (titleTds != null && !titleTds.isEmpty()) {
                    String newsCategory = titleTds.get(0).asText().trim();
                    Optional<NewsType> optional = Arrays.stream(NewsType.values())
                                                        .filter(x -> newsCategory.equals(x.getName()))
                                                        .findFirst();
                    if (optional.isPresent()) {
                        NewsType newsType = optional.get();
                        HtmlElement titleElement = element.getElementsByAttribute("td", "class", "ntitle").get(0);
                        String newsURL = titleElement.getElementsByTagName("a").get(0).getAttribute("href");
                        page = client.getPage(newsURL);
                        List<HtmlElement> newsElements = page.getByXPath("//*[@id=\"dle-content\"]");
                        if (newsElements != null && !newsElements.isEmpty()) {
                            HtmlElement newsElement = newsElements.get(0);
                            String newsTitle = ((HtmlElement) newsElement.getByXPath("//table/tbody/tr/td/table/tbody/tr/td[@class=\"ntitle\"]").get(0)).getTextContent();

                            HtmlElement newsBodyHtmlElement = (HtmlElement) newsElement.getByXPath("//div[contains(@id, \"news-id\")]").get(0);
                            String newsBody = newsBodyHtmlElement.asText();

                            ArrayList<String> lines = new ArrayList<>(Arrays.asList(newsBody.split("\r\n")));
                            lines.removeAll(Collections.singleton(""));

                            StringBuilder newsGender = new StringBuilder();
                            Optional<String> gender = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Стиль")).findFirst();
                            gender.ifPresent(newsGender::append);

                            if (newsGender.length() == 0) {
                                gender = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Жанр")).findFirst();
                                gender.ifPresent(newsGender::append);
                            }

                            StringBuilder newsCountry = new StringBuilder();
                            StringBuilder newsFormat = new StringBuilder();
                            if (newsType != NewsType.Concerts) {
                                Optional<String> country = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Страна")).findFirst();
                                country.ifPresent(newsCountry::append);
                                if (newsCountry.length() == 0) {
                                    country = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Родина")).findFirst();
                                    country.ifPresent(newsCountry::append);
                                }

                                Optional<String> format = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Формат")).findFirst();
                                format.ifPresent(newsFormat::append);

                                if (newsFormat.length() == 0) {
                                    format = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Качество")).findFirst();
                                    format.ifPresent(newsFormat::append);
                                }
                            }

                            String tracks = getTrackList(lines);

                            String newsImageUrl = "";
                            String href = "";
                            List<HtmlElement> aElements = newsBodyHtmlElement.getElementsByTagName("a");
                            if (aElements != null && !aElements.isEmpty()) {
                                newsImageUrl = getImageUrl(aElements);
                                href = getHref(aElements);
                            }
                            if (newsImageUrl == null || newsImageUrl.isEmpty()) {
                                newsImageUrl = getImageSrc(newsBodyHtmlElement.getElementsByTagName("img"));
                            }

                            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
                            DateTime dateTime = DateTime.now();
                            List<HtmlElement> commentElements = newsElement.getByXPath("//*[@id=\"dle-content\"]/table/tbody/tr/td/div[@class=\"slink1\"]");
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
                            System.out.println(formatter.print(dateTime));

                            System.out.println("Add news");
                            News news = new News(newsTitle, newsType, dateTime, newsGender.toString(), newsFormat.toString(), newsCountry.toString(), tracks, href, newsImageUrl);
                            System.out.println(news);
                            System.out.println("--------------------------------------------------------");
                            newsArrayList.add(news);
                        }

                    }
                }
            }
            TimeUnit.MINUTES.sleep(1);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private static String getTrackList(ArrayList<String> lines) {
        StringBuilder tracks = new StringBuilder("");
        for (int i = 0; i < lines.size(); i++) {
            if (StringUtils.contains(lines.get(i), "Треклист") || StringUtils.contains(lines.get(i), "Tracklist")) {
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

    private static String getImageUrl(List<HtmlElement> aElements) {
        List<HtmlElement> imageElements = aElements.get(0).getElementsByTagName("img");
        return getImageSrc(imageElements);
    }

    private static String getImageSrc(List<HtmlElement> imageElements) {
        if (imageElements != null && !imageElements.isEmpty()) {
            HtmlElement imageElement = imageElements.get(0);
            if (imageElement != null) {
                return imageElement.getAttribute("src");
            }
        }
        return "";

    }

    private static String getHref(List<HtmlElement> elements) {
        HtmlElement downloadElement = elements.get(elements.size() - 1);
        if (downloadElement != null) {
            return downloadElement.getAttribute("href");
        }
        return "";
    }
}
