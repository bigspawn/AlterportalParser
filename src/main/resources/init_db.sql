CREATE SEQUENCE public.news_type_ids;
CREATE TABLE public.news_type (
  id_news_type INT PRIMARY KEY DEFAULT NEXTVAL('news_type_ids'),
  name         VARCHAR(100)
);

INSERT INTO public.news_type (id_news_type, name) VALUES (1, 'Новости');
INSERT INTO public.news_type (id_news_type, name) VALUES (2, 'Альбомы 2017');
INSERT INTO public.news_type (id_news_type, name) VALUES (3, 'Alternative');
INSERT INTO public.news_type (id_news_type, name) VALUES (4, 'Punk');
INSERT INTO public.news_type (id_news_type, name) VALUES (5, 'Emo / Hardcore');
INSERT INTO public.news_type (id_news_type, name) VALUES (6, 'Metal');
INSERT INTO public.news_type (id_news_type, name) VALUES (7, 'Industrial');
INSERT INTO public.news_type (id_news_type, name) VALUES (8, 'Rock');
INSERT INTO public.news_type (id_news_type, name) VALUES (9, 'Experimental');
INSERT INTO public.news_type (id_news_type, name) VALUES (11, 'Новые треки');
INSERT INTO public.news_type (id_news_type, name) VALUES (12, 'Концерты');
INSERT INTO public.news_type (id_news_type, name) VALUES (10, 'Аудио CD');

CREATE SEQUENCE public.news_ids;

CREATE TABLE public.news (
  id_news      INT PRIMARY KEY DEFAULT NEXTVAL('news_ids'),
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