package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
@Getter
@Setter
public class UrlCheck extends Model {
    @Id
    private long id;
    private int statusCode;
    private String title;
    private String h1;
    @Lob
    private String description;
    @ManyToOne
    private Url url;
    @WhenCreated
    private Instant createdAt;
}
