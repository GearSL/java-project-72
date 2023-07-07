package domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
@Getter
@Setter
public class Url extends Model {
    @Id
    private long id;
    String name;
    @WhenCreated
    private Instant createdAt;

    public Url(String siteName) {
        name = siteName;
    }
}
