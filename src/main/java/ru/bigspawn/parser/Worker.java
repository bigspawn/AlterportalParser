package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ru.bigspawn.parser.bot.MyBot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.bigspawn.parser.Main.logger;

/**
 * Created by bigspawn on 09.06.2017.
 */
public class Worker implements Runnable {
    public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
    private static final String INSERT_NEWS = "INSERT INTO news_test " +
            "(title, id_news_type, date, gender, format, country, playlist, download_url, image_url) " +
            "VALUES (?, (SELECT id_news_type FROM news_type WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_NEWS = "SELECT * FROM news_test WHERE title = ?";

    private MyBot bot;
    private WebClient client;
    private HtmlPage page;
    private int pageNumber = 1;
    private String url;
    private Connection connection;
    private boolean key;

    public Worker(MyBot bot) throws UnsupportedEncodingException {
        this.bot = bot;
        this.client = new WebClient();
        this.url = getPageURL();
        setOptions();
        createConnection();
    }

    private void createConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:15432/alterportal_news","bigspawn", "52169248");
        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e, e);
        }
    }

    private String getPageURL() throws UnsupportedEncodingException {
        return Configs.getInstance().getURL() + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + "/";
    }

    private void setOptions() {
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
    }

    @Override
    public void run() {
        logger.info("Start worker");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                page = client.getPage(url);
                List<HtmlElement> elements = page.getByXPath("//*[@id=\"dle-content\"]/table/tbody/tr/td/table");
                logger.info("Get " + elements.size() + " news from page № " + pageNumber);
                for (HtmlElement element : elements) {
                    List<HtmlElement> titleTds = element.getElementsByAttribute("td", "class", "category");
                    if (titleTds != null && !titleTds.isEmpty()) {
                        String newsCategory = titleTds.get(0).asText().trim();
                        Optional<NewsType> optional = Arrays.stream(NewsType.values()).filter(x -> newsCategory.equals(x.getName())).findFirst();
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

                                String newsGender = "";
                                Optional<String> gender = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Стиль")).findFirst();
                                if (gender.isPresent()) {
                                    newsGender += gender.get();
                                }

                                if (newsGender.length() == 0) {
                                    gender = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Жанр")).findFirst();
                                    if (gender.isPresent()) {
                                        newsGender += gender.get();
                                    }
                                }

                                newsGender = newsGender.replace(":: ::", ":");
                                newsGender = newsGender.replace("::", "");
                                newsGender = newsGender.trim();

                                String newsCountry = "";
                                String newsFormat = "";
                                if (newsType != NewsType.Concerts) {
                                    Optional<String> country = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Страна")).findFirst();
                                    if (country.isPresent()) {
                                        newsCountry += country.get();
                                    }
                                    if (newsCountry.length() == 0) {
                                        country = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Родина")).findFirst();
                                        if (country.isPresent()) {
                                            newsCountry += country.get();
                                        }
                                    }
                                    newsCountry = newsCountry.replace(":: ::", ":");
                                    newsCountry = newsCountry.replace("::", "");
                                    newsCountry = newsCountry.trim();

                                    Optional<String> format = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Формат")).findFirst();
                                    if (format.isPresent()) {
                                        newsFormat += format.get();
                                    }
                                    if (newsFormat.length() == 0) {
                                        format = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Качество")).findFirst();
                                        if (format.isPresent()) {
                                            newsFormat += format.get();
                                        }
                                    }
                                    newsFormat = newsFormat.replace(":: ::", ":");
                                    newsFormat = newsFormat.replace("::", "");
                                    newsFormat = newsFormat.trim();
                                }

                                String tracks = getTrackList(lines);
                                String newsImageUrl = "";
                                String href = "";
                                List<HtmlElement> aElements = newsBodyHtmlElement.getElementsByTagName("a");
                                if (aElements != null && !aElements.isEmpty()) {
                                    newsImageUrl = getImageUrl(aElements);
                                    href = getHref(aElements);
                                }
                                if (newsImageUrl == null || newsImageUrl.isEmpty() || !newsImageUrl.contains("fastpic")
                                        || !newsImageUrl.contains("radikal")) {
                                    newsImageUrl = getImageSrc(newsBodyHtmlElement.getElementsByTagName("img"));
                                }


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
                                News news = new News(newsTitle, newsType, dateTime, newsGender, newsFormat, newsCountry, tracks, href, newsImageUrl);
                                logger.info("Create new item: " + news);

                                PreparedStatement ps = null;
                                PreparedStatement ps2 = null;
                                try {
                                    ps = connection.prepareStatement(SELECT_NEWS);
                                    ps.setString(1, newsTitle);
                                    ResultSet rs = ps.executeQuery();
                                    if (!rs.next()) {
                                        ps2 = connection.prepareStatement(INSERT_NEWS);
                                        ps2.setString(1, newsTitle);
                                        ps2.setString(2, newsType.getName());
                                        ps2.setTimestamp(3, new Timestamp(dateTime.getMillis()));
                                        ps2.setString(4, newsGender);
                                        ps2.setString(5, newsFormat);
                                        ps2.setString(6, newsCountry);
                                        ps2.setString(7, tracks);
                                        ps2.setString(8, href);
                                        ps2.setString(9, newsImageUrl);
                                        ps2.execute();
                                        logger.info("Add news: " + news.getTitle() + " - to DB");
                                        bot.sendNewsToChanel(news, Configs.getInstance().getTELEGRAM_CHANEL());
                                    } else {
                                        key = true;
                                        break;
                                    }
                                } catch (SQLException e) {
                                    logger.error(e, e);
                                } finally {
                                    if (ps != null) {
                                        try {
                                            ps.close();
                                        } catch (SQLException e) {
                                            logger.error(e, e);
                                        }
                                    }
                                    if (ps2 != null) {
                                        try {
                                            ps2.close();
                                        } catch (SQLException e) {
                                            logger.error(e, e);
                                        }
                                    }
                                }
                                logger.info("Sleep 10 seconds");
                                TimeUnit.SECONDS.sleep(10);
                            }
                        }
                    }
                }
                if (key) {
                    pageNumber = 1;
                    url = getPageURL();
                    key = false;
                    logger.info("Has not new news. Sleep 30 minutes");
                    TimeUnit.MINUTES.sleep(30);
                } else {
                    pageNumber++;
                    url = getPageURL();
                }
            } catch (InterruptedException | IOException e) {
                logger.error(e, e);
            }
        }
        logger.info("Stop worker");
    }


    private String getTrackList(ArrayList<String> lines) {
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
        HtmlElement downloadElement = elements.get(elements.size() - 1);
        if (downloadElement != null) {
            return downloadElement.getAttribute("href");
        }
        return "";
    }
}
