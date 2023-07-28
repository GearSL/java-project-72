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
        URL urlParser = null;

        try {
            urlParser = new URL(inputUrl);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Укажите корректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }

        int port = urlParser == null ? -1 : urlParser.getPort();
        String host = urlParser == null ? "" : urlParser.getHost();
        String protocol = urlParser == null ? "https" : urlParser.getProtocol();

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

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("sites/show.html");
    };
}
