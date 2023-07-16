package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import hexlet.code.dto.UrlCheckDto;
import hexlet.code.service.UrlParserServiceImpl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CheckController {
    public static Handler addCheck = ctx -> {
        UrlParserServiceImpl urlParserService = new UrlParserServiceImpl();

        long urlId = Long.parseLong(ctx.pathParam("id"));
        Url url = new QUrl().id.equalTo(urlId).findOne();

        if (url == null) {
            throw new NotFoundResponse("Не найден URL для которого следует выполнить проверку");
        }

        try {
            UrlCheckDto urlCheckDto = urlParserService.parseUrl(url);
            urlParserService.saveCheck(urlCheckDto);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

            ctx.redirect("/urls/" + urlId);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }
    };
}
