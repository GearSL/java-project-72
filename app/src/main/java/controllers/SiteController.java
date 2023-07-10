package controllers;

import domain.Url;
import domain.UrlCheck;
import domain.query.QUrl;
import domain.query.QUrlCheck;
import io.javalin.http.Handler;
import java.net.URL;
import java.util.List;

public final class SiteController {
    public static Handler addSite = ctx -> {
        String fullUrl = ctx.formParam("url");
        String protocol;
        String host;
        int port;

        if (fullUrl != null && !fullUrl.isEmpty()) {
            URL urlParser = new URL(fullUrl);
            port = urlParser.getPort();
            host = urlParser.getHost();
            protocol = urlParser.getProtocol();
        } else {
            ctx.sessionAttribute("flash", "Укажите корректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        String resultName = protocol + "://" + host + (port == -1 ? "" : ":" + port);
        Url url = new Url(resultName);
        List<Url> similarUrls = new QUrl().where().name.contains(host).findList();

        if (similarUrls.size() >= 1) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
        } else {
            url.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        List<Url> urls = new QUrl().orderBy()
                .id.asc()
                .findList();

        ctx.attribute("urls", urls);
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
        Url url = new QUrl().id.equalTo(id).findOne();
        List<UrlCheck> urlChecks = new QUrlCheck().url.equalTo(url).findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("sites/show.html");
    };
}
