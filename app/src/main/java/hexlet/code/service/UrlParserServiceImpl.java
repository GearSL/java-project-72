package hexlet.code.service;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.dto.UrlCheckDto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class UrlParserServiceImpl implements UrlParserService {
    @Override
    public UrlCheckDto parseUrl(Url url) throws Exception {
        try {
            HttpResponse<String> response = Unirest.get(url.getName())
                    .header("Content-Type", "application/json")
                    .asString();
            String htmlBody = response.getBody();
            int statusCode = response.getStatus();
            Document document = Jsoup.parse(htmlBody);
            Element h1Element = document.select("h1").first();
            String h1Tag = h1Element == null ? "" : h1Element.text();
            Element titleElement = document.select("title").first();
            String titleTag = titleElement == null ? "" : titleElement.text();
            Element descriptionElement = document.select("meta[name=description]").first();
            String descriptionTag = descriptionElement == null ? "" : descriptionElement.attr("content");

            UrlCheckDto urlCheckDto = new UrlCheckDto();
            urlCheckDto.setUrl(url);
            urlCheckDto.setStatusCode(statusCode);
            urlCheckDto.setTitle(titleTag);
            urlCheckDto.setH1(h1Tag);
            urlCheckDto.setDescription(descriptionTag);

            return urlCheckDto;
        } catch (UnirestException e) {
            throw new UnirestException("Некорректный адрес");
        } catch (Exception e) {
            throw new Exception("Something went wrong");
        }
    }

    @Override
    public void saveCheck(UrlCheckDto urlCheckDto) {
        UrlCheck urlCheck = new UrlCheck();
        urlCheck.setUrl(urlCheckDto.getUrl());
        urlCheck.setStatusCode(urlCheckDto.getStatusCode());
        urlCheck.setTitle(urlCheckDto.getTitle());
        urlCheck.setH1(urlCheckDto.getH1());
        urlCheck.setDescription(urlCheckDto.getDescription());
        urlCheck.save();
    }
}
