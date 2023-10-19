package com.doutoutdou.urlshortener.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.doutoutdou.urlshortener.utils.Constants.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UrlRequestDto {

    @NotBlank(message = URL_IS_REQUIRED)
    @Size(min = 15, max = 2048, message = URL_LENGTH)
    @Pattern(regexp = "^(http|https)://.*$", message = URL_PATTERN)
    private String url;
}
