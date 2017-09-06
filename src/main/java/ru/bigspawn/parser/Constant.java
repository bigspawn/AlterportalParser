package ru.bigspawn.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constant {

  public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("dd MMMM yyyy");
  public static final List<String> DOWNLOAD_RESOURCES = Collections
      .unmodifiableList(new ArrayList<String>() {
    {
      add("mail.ru");
      add("yadi.sk");
      add("rgho.st");
      add("mystore.to");
      add("mediafire.com");
      add("zippyshare.com");
      add("mega.nz");
      add("solidfiles.com");
      add("adrive.com");
      add("firedrive.com");
      add("my-files.ru");
    }
      });
}
