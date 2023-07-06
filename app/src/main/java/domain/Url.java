package domain;

import io.ebean.annotation.WhenCreated;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url {
    @Id
    private long id;
    String name;
    @WhenCreated
    private Instant createdAt;
}
