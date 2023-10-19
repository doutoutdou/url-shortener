package com.doutoutdou.urlshortener.exception;

import lombok.Getter;

@Getter
public class ShortenedUrlNotFoundException extends Exception {
    public ShortenedUrlNotFoundException(String message) {
        super(message);
    }

}
