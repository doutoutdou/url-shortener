package com.doutoutdou.urlshortener.exception;

import lombok.Getter;

@Getter
public class NoShortenedUrlAvailableException extends RuntimeException {
    public NoShortenedUrlAvailableException(String message) {
        super(message);
    }

}
