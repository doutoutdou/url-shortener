package com.doutoutdou.urlshortener.controller;

import com.doutoutdou.urlshortener.dto.ShortenedUrlRequestDto;
import com.doutoutdou.urlshortener.dto.ShortenedUrlResponseDto;
import com.doutoutdou.urlshortener.dto.UrlRequestDto;
import com.doutoutdou.urlshortener.dto.UrlResponseDto;
import com.doutoutdou.urlshortener.exception.ShortenedUrlNotFoundException;
import com.doutoutdou.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UrlController implements UrlOperations {

    private final UrlService urlService;

    @Override
    public ResponseEntity<UrlResponseDto> getFromShortenedUrl(ShortenedUrlRequestDto shortenedUrlRequestDto) throws ShortenedUrlNotFoundException {
        return ResponseEntity.ok(urlService.findFromShortenedUrl(shortenedUrlRequestDto));
    }

    @Override
    public ResponseEntity<ShortenedUrlResponseDto> createShortenedUrl(UrlRequestDto dto) {
        return ResponseEntity.ok().body(urlService.buildShortenedUrl(dto));
    }
}
