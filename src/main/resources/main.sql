CREATE TABLE news_test (
  id_news      INT PRIMARY KEY DEFAULT NEXTVAL('news_test_ids'),
  title        VARCHAR(255),
  id_news_type INT REFERENCES news_type (id_news_type),
  date         TIMESTAMP,
  gender       VARCHAR(255),
  format       VARCHAR(255),
  country      VARCHAR(255),
  playlist     TEXT,
  download_url VARCHAR(255),
  image_url    VARCHAR(255)
);


CREATE SEQUENCE news_test_ids;
CREATE SEQUENCE news_type_ids;

CREATE TABLE news_type (
  id_news_type INT PRIMARY KEY DEFAULT NEXTVAL('news_type_ids'),
  name         VARCHAR(100)
);

INSERT INTO news_type (name)
VALUES ('Альбомы 2017');
INSERT INTO news_type (name)
VALUES ('Alternative');
INSERT INTO news_type (name)
VALUES ('Punk');
INSERT INTO news_type (name)
VALUES ('Emo / Hardcore');
INSERT INTO news_type (name)
VALUES ('Metal');
INSERT INTO news_type (name)
VALUES ('Industrial');
INSERT INTO news_type (name)
VALUES ('Rock');
INSERT INTO news_type (name)
VALUES ('Experimental');
INSERT INTO news_type (name)
VALUES ('Аудио CD (lossless)');
INSERT INTO news_type (name)
VALUES ('Новые треки');
INSERT INTO news_type (name)
VALUES ('Концерты');