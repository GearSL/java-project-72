package controllers;

import domain.Url;
import domain.query.QUrl;
import io.javalin.http.Handler;
import java.net.URL;
import java.util.List;

public final class SiteController {
    public static Handler addSite = ctx -> {
        Url url = new Url();
        String fullUrl = ctx.formParam("url");
        String protocol = "";
        String host = "";
        int port = 0;

        if (fullUrl != null) {
            URL urlParser = new URL(fullUrl);
            port = urlParser.getPort();
            host = urlParser.getHost();
            protocol = urlParser.getProtocol();
        }
        String resultName = protocol + "://" + host + (port == -1 ? "" : ":" + port);
        url.setName(resultName);
        List<Url> similarUrls = new QUrl().where().name.contains(host).findList();

        if (similarUrls.size() > 1) {
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
        ctx.render("sites/index.html");
    };

    public static Handler showSitesList = ctx -> {
      List<Url> urls = new QUrl().findList();
      ctx.attribute("urls", urls);
      ctx.render("sites/index.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl().id.equalTo(id).findOne();
        ctx.attribute("url", url);
        ctx.render("sites/show.html");
    };
}
