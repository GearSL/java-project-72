package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@AllArgsConstructor
public class CheckController {
    public static Handler addCheck = ctx -> {
        long urlId = Long.parseLong(ctx.pathParam("id"));
        Url url = new QUrl().id.equalTo(urlId).findOne();

        if (url == null) {
            throw new NotFoundResponse("Не найден URL для которого следует выполнить проверку");
        }

        try {
            HttpResponse<String> response = Unirest.get(url.getName())
                    .header("Content-Type", "application/json").asString();
            String htmlBody = response.getBody();
            int statusCode = response.getStatus();
            Document document = Jsoup.parse(htmlBody);
            Element h1Element = document.select("h1").first();
            String h1Tag = h1Element == null ? "" : h1Element.text();
            Element titleElement = document.select("title").first();
            String titleTag = titleElement == null ? "" : titleElement.text();
            Element descriptionElement = document.select("meta[name=description]").first();
            String descriptionTag = descriptionElement == null ? "" : descriptionElement.attr("content");

            new UrlCheck(statusCode, titleTag, h1Tag, descriptionTag, url).save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + urlId);
    };
}
