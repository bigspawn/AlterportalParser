package ru.bigspawn.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  public static String getLoggerNameFromUrl(String url) {
    Pattern pattern = Pattern.compile("/(\\w+)/");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group().replaceAll("/" , "");
    }
    return url;
  }
}
