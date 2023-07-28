package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlCheckDto {
    private int statusCode;
    private String title;
    private String h1;
    private String description;
}
