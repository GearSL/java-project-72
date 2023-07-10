package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Objects;

public class CheckController {
    public static Handler addCheck = ctx -> {
        String firstH1Tag = "";
        String title = "";
        String description = "";
        int statusCode = 0;

        long urlId = Long.parseLong(ctx.pathParam("id"));
        Url url = new QUrl().id.equalTo(urlId).findOne();

        if (url != null) {
            HttpResponse<String> response = Unirest.get(url.getName())
                    .header("Content-Type", "application/json")
                    .asString();
            String htmlBody = response.getBody();
            Document document = Jsoup.parse(htmlBody);

            statusCode = response.getStatus();
            firstH1Tag = Objects.requireNonNull(document.select("h1").first()).text();
            title = Objects.requireNonNull(document.select("title").first()).text();
            description = Objects.requireNonNull(document.select("meta[name=description]")
                    .first()).attr("content");


            UrlCheck urlCheck = new UrlCheck();
            urlCheck.setUrl(url);
            urlCheck.setStatusCode(statusCode);
            urlCheck.setTitle(title);
            urlCheck.setH1(firstH1Tag);
            urlCheck.setDescription(description);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } else {
            ctx.sessionAttribute("flash", "Не найден URL для которого следует выполнить проверку");
        }

        ctx.redirect("/urls/" + urlId);
    };
}
