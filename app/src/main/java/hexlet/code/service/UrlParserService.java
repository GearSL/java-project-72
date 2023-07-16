package hexlet.code.service;

import hexlet.code.domain.Url;
import hexlet.code.dto.UrlCheckDto;

public interface UrlParserService {
    UrlCheckDto parseUrl(Url url) throws Exception;
    void saveCheck(UrlCheckDto urlCheckDto);
}
