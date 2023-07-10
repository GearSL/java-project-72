package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
public class Url extends Model {
    @Id
    private long id;
    String name;
    @WhenCreated
    private Instant createdAt;
    @OneToMany
    List<UrlCheck> urlChecks;

    public Url(String siteName) {
        name = siteName;
    }
}
