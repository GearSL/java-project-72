package hexlet.code;

import domain.Url;
import domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url("https://example.com");
        existingUrl.save();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }

        @Test
        void testUrls() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Сайты");
        }
    }

    @Nested
    class UrlTest {

        @Test
        void testIndexUrls() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());
        }

        @Test
        void createSite() {
            String newSite = "https://sometestsite.com/somepage";
            String hostName = "https://sometestsite.com";
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", newSite)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(hostName)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(hostName);
        }
    }

    @Nested
    class CheckTest {
        @Test
        public void createSuccessfulRequest() {
            MockWebServer server = new MockWebServer();
            server.url(existingUrl.toString());
            server.enqueue(new MockResponse().setBody("hello, world!"));

            HttpUrl appendUrl = server.url("/urls/1/checks");
            HttpResponse responsePost = Unirest
                    .post(appendUrl.toString())
                    .asString();
            assertThat(responsePost.getBody()).isEqualTo("hello, world!");
        }

        @Test
        public void createBadRequest() throws IOException {
            MockWebServer server = new MockWebServer();
            server.url(existingUrl.toString()).toString();
            server.enqueue(new MockResponse().setBody("hello, world!"));

            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls/2/checks")
                    .asString();
            assertThat(responsePost.getStatus()).isEqualTo(302);
        }
    }
}
