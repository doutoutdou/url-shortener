package com.doutoutdou.urlshortener.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.doutoutdou.urlshortener.utils.Constants.SHORTENED_URL_PARAMETER_FORMAT;
import static com.doutoutdou.urlshortener.utils.Constants.SHORTENED_URL_PATTERN;

@AllArgsConstructor
@Getter
public class ShortenedUrlRequestDto {

    @Pattern(regexp = SHORTENED_URL_PATTERN, message = SHORTENED_URL_PARAMETER_FORMAT)
    private String shortenedUrl;

}
