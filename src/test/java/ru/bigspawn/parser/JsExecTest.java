package ru.bigspawn.parser;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

public class JsExecTest {

  public static final String IMAGE_URL = "http://i100.fastpic.ru/big/2017/1207/d2/43994ebf8e2d310fa2398c4ac21791d2.jpg";
  public static final String IMAGE_URL2 = "https://1.bp.blogspot.com/-5bPNsF5plzw/VnJWs-7RbrI/AAAAAAAARmA/DaZmn8YUjAk/s1600-r/logo_research_at_google_color_1x_web_512dp.png";

  @Test
  public void test() {

    try (WebClient webClient = new WebClient(BrowserVersion.FIREFOX_52)) {
      webClient.getOptions().setJavaScriptEnabled(true);
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setCssEnabled(true);
      webClient.getOptions().setUseInsecureSSL(true);
      webClient.getOptions().setRedirectEnabled(true);
      Page page = webClient.getPage(IMAGE_URL);
      System.out.println("Page 1 = " + page);

      page = webClient.getPage(page.getUrl());
      System.out.println("Page 2 = " + page);

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void httpTest() throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(IMAGE_URL).build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Failed to download file: " + response);
    }
    FileOutputStream fos = new FileOutputStream("a.jpg");
    fos.write(response.body().bytes());
    fos.close();
  }

  @Test
  public void downloadFile() throws IOException {
    String destinationFile = "image.jpg";
    URL url = new URL(IMAGE_URL);
    System.out.println(url);
    BufferedImage img = ImageIO.read(url);
    File file = new File(destinationFile);
    ImageIO.write(img, "jpg", file);
  }

  @Test
  public void main() {
    try {
      URL obj = new URL(IMAGE_URL);
      HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      conn.addRequestProperty("User-Agent", "Mozilla");
      conn.addRequestProperty("Referer", "google.com");

      System.out.println("Request URL ... " + IMAGE_URL);

      boolean redirect = false;

      // normally, 3xx is redirect
      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_SEE_OTHER) {
          redirect = true;
        }
      }

      System.out.println("Response Code ... " + status);

      if (redirect) {

        // get redirect url from "location" header field
        String newUrl = conn.getHeaderField("Location");

        // get the cookie if need, for login
        String cookies = conn.getHeaderField("Set-Cookie");

        // open the new connnection again
        conn = (HttpURLConnection) new URL(newUrl).openConnection();
        conn.setRequestProperty("Cookie", cookies);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", "Mozilla");
        conn.addRequestProperty("Referer", "google.com");

        System.out.println("Redirect to URL : " + newUrl);

      }

      BufferedReader in = new BufferedReader(
          new InputStreamReader(conn.getInputStream()));
      String inputLine;
      StringBuffer html = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        html.append(inputLine);
      }
      in.close();

      System.out.println("URL Content... \n" + html.toString());
      System.out.println("Done");

    } catch (Exception e) {
      e.printStackTrace();
    }

  }


}
