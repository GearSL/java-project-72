package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;

public final class SiteController {
    public static Handler addSite = ctx -> {
        String inputUrl = ctx.formParam("url");
        String protocol = "";
        String host = "";
        int port = 0;

        URL urlParser;

        try {
            urlParser = new URL(inputUrl);
            port = urlParser.getPort();
            host = urlParser.getHost();
            protocol = urlParser.getProtocol();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Укажите корректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }
        String resultName = protocol + "://" + host + (port == -1 ? "" : ":" + port);
        Url url = new Url(resultName);
        Url similarUrl = new QUrl()
                .name.equalTo(host)
                .findOne();

        if (similarUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
        } else {
            url.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler showSitesList = ctx -> {
        List<Url> urls = new QUrl()
                .findList();
        ctx.attribute("urls", urls);
        ctx.render("sites/index.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .urlChecks.fetch()
                .orderBy()
                .urlChecks.createdAt.desc()
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlChecks = new QUrlCheck().url.equalTo(url).findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("sites/show.html");
    };
}