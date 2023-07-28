package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;
    private static MockWebServer mockServer;
    private static String mockServerUrl;


    private static Path getFixturePath(String fileName) {
        return Paths.get("src", "test", "resources", "fixtures", fileName)
                .toAbsolutePath().normalize();
    }

    private static String readFixture(String fileName) throws IOException {
        Path filePath = getFixturePath(fileName);
        return Files.readString(filePath).trim();
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        mockServer = new MockWebServer();
        String htmlString = readFixture("index.html");
        MockResponse mockedResponse = new MockResponse().setBody(htmlString);
        mockServer.enqueue(mockedResponse);
        mockServer.start();
        mockServerUrl = mockServer.url("/").toString().replaceAll("/$", "");

        Url existedUrl = new Url("https://test.com");
        existedUrl.save();
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
            assertThat(body).contains(mockServerUrl);
        }

        @Test
        void testAttemptOpenNonExistentWebsite() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/99")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(body).contains("Not Found");
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
            Unirest.post(baseUrl + "/urls")
                    .field("url", mockServerUrl)
                    .asEmpty();

            Url findedUrl = new QUrl().name.equalTo(mockServerUrl).findOne();
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls/" + findedUrl.getId() + "/checks")
                    .asString();

            UrlCheck check = new QUrlCheck().url.equalTo(findedUrl).findOne();
            assertThat(check.getTitle()).isEqualTo("Example title");
            assertThat(check.getDescription())
                    .contains("GitHub is where over 100 million developers");
            assertThat(check.getH1()).isEqualTo("Test page");
            assertThat(responsePost.getStatus()).isEqualTo(302);
        }

        @Test
        public void createBadRequest() {
            HttpResponse responsePost = Unirest
                    //999 - identifier which can't be in our DB
                    .post(baseUrl + "/urls/999/checks")
                    .asString();
            assertThat(responsePost.getStatus()).isEqualTo(404);
        }
    }
}
