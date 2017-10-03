package ru.bigspawn.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  public static String getLoggerNameFromUrl(String url) {
    Pattern pattern = Pattern.compile("/(\\w+)/");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group().replaceAll("/", "");
    }
    return url;
  }

  public static void appendIfNotNPE(StringBuilder builder, String... tags) {
    if (tags != null && tags.length > 0) {
      for (String tag : tags) {
        if (tag != null && !tag.isEmpty()) {
          builder.append(tag).append('\n');
        }
      }
    }
  }
}
