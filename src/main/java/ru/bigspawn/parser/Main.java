package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bigspawn on 30.05.2017.
 */
public class Main {
    public static void main(String[] args) {
        int pageNumber = 1;

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        try {
            String url = "http://alterportal.ru/page/" + URLEncoder.encode(String.valueOf(pageNumber), "UTF-8") + "/";
            HtmlPage page = client.getPage(url);
            List<HtmlElement> items = page.getByXPath("//div[contains(@id,'news-id')]");
            if (items.isEmpty()) {
                System.out.println("No items found !");
            } else {
                for (HtmlElement item : items) {
                    System.out.println(item.asText());
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
