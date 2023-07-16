package hexlet.code.dto;

import hexlet.code.domain.Url;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Getter
@Setter
public class UrlCheckDto {
    private int statusCode;
    private String title;
    private String h1;
    @Lob
    private String description;
    @ManyToOne
    private Url url;
}
