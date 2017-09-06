package ru.bigspawn.parser.parser;

import java.io.IOException;
import java.util.List;
import ru.bigspawn.parser.entity.News;

public interface Parser {

  List<News> parse(int pageNumber) throws IOException;

}
