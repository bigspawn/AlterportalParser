package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {
    private static ArrayList<News> newsArrayList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
//        try {
//            Class.forName("org.h2.Driver");
//            Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "admin", "52169248");
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
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
                    String newsCategory = titleTds.get(0).asText();
                    Optional<NewsType> optional = Arrays.stream(NewsType.values())
                                                        .filter(x -> newsCategory.equals(x.getName()))
                                                        .findFirst();
                    if (optional.isPresent()) {
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

                            StringBuilder newsCountry = new StringBuilder();
                            Optional<String> country = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Страна")).findFirst();
                            country.ifPresent(newsCountry::append);

                            StringBuilder newsFormat = new StringBuilder();
                            Optional<String> format = lines.stream().filter(line -> StringUtils.containsIgnoreCase(line, "Формат")).findFirst();
                            format.ifPresent(newsFormat::append);

                            StringBuilder tracks = new StringBuilder();
                            for (int i = 0; i < lines.size(); i++) {
                                if (StringUtils.contains(lines.get(i), "Треклист")) {
                                    for (int j = i + 1; j < lines.size(); j++) {
                                        String track = lines.get(j);
                                        if (Character.isDigit(track.charAt(0))) {
                                            tracks.append(track).append(System.lineSeparator());
                                        }
                                    }
                                    break;
                                }
                            }

                            String newsImageURL = "";
                            String href = "";
                            List<HtmlElement> aElements = newsBodyHtmlElement.getElementsByTagName("a");
                            if (aElements != null && !aElements.isEmpty()) {
                                List<HtmlElement> imageElements = aElements.get(0).getElementsByTagName("img");
                                if (imageElements != null && !imageElements.isEmpty()) {
                                    newsImageURL = imageElements.get(0).getAttribute("src");
                                }
                                HtmlElement downloadElement = aElements.get(aElements.size() - 1);
                                href = downloadElement.getAttribute("href");
                            }


                            System.out.println("Add news");
                            News news = new News(newsTitle, newsCategory, null, null, null,
                                    newsGender.toString(), newsFormat.toString(), newsCountry.toString(), tracks.toString(), href, newsImageURL);
                            System.out.println(news);
                            System.out.println("--------------------------------------------------------");
                            newsArrayList.add(news);
                        }

                    }
                }
            }
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
