package com.doutoutdou.urlshortener.service;

import com.doutoutdou.urlshortener.dto.ShortenedUrlRequestDto;
import com.doutoutdou.urlshortener.dto.ShortenedUrlResponseDto;
import com.doutoutdou.urlshortener.dto.UrlRequestDto;
import com.doutoutdou.urlshortener.dto.UrlResponseDto;
import com.doutoutdou.urlshortener.entity.Url;
import com.doutoutdou.urlshortener.exception.NoShortenedUrlAvailableException;
import com.doutoutdou.urlshortener.exception.ShortenedUrlNotFoundException;
import com.doutoutdou.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.doutoutdou.urlshortener.utils.Constants.SHORTENED_URL_BASE_PATH;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    private static final int SHORT_HASH_SIZE = 10;

    private final UrlRepository urlRepository;
    private final HashService hashService;

    /**
     * Search an url from its shortened version
     *
     * @param shortenedUrlRequestDto the shortened url object
     * @return the original url if found
     * @throws ShortenedUrlNotFoundException if the shortened url has not been found
     */
    public UrlResponseDto findFromShortenedUrl(ShortenedUrlRequestDto shortenedUrlRequestDto) throws ShortenedUrlNotFoundException {
        // We need to delete the url base path
        var shortenedHash = shortenedUrlRequestDto.getShortenedUrl().substring(SHORTENED_URL_BASE_PATH.length());

        return urlRepository.findByShortenedHash(shortenedHash)
                .map(url -> new UrlResponseDto(URLDecoder.decode(url.getOriginUrl(), StandardCharsets.UTF_8)))
                .orElseThrow(() -> {
                    log.error(STR. "No Url found for the shortenedHash \{ shortenedHash }" );
                    return new ShortenedUrlNotFoundException(STR."Shortened url not found");
                });
    }

    /**
     * Create a shortened url
     *
     * @param urlRequestDto the original url object
     * @return a shortened url
     */
    public ShortenedUrlResponseDto buildShortenedUrl(UrlRequestDto urlRequestDto) {
        var hash = hashService.createHash(urlRequestDto.getUrl());

        // Search if the shortened url is already present
        // If not create a new one
        var shortenedHash = urlRepository.findByHash(hash).map(Url::getShortenedHash)
                .orElseGet(() -> createAndSaveUrl(hash, urlRequestDto.getUrl()));

        return new ShortenedUrlResponseDto(SHORTENED_URL_BASE_PATH + shortenedHash);
    }

    /**
     * Create an url entity and save it
     *
     * @param hash      the originUrl hash
     * @param originUrl the originUrl
     * @return the short hash generated
     */
    private String createAndSaveUrl(String hash, String originUrl) {
        var url = Url.builder()
                .hash(hash)
                .originUrl(URLEncoder.encode(originUrl, StandardCharsets.UTF_8))
                .shortenedHash(computeShortenedHash(hash, 0, SHORT_HASH_SIZE))
                .build();

        return urlRepository.save(url).getShortenedHash();
    }

    /**
     * Create a short hash (10 characters) from original hash
     * if the short hash is already existing, try to create a new one
     *
     * @param hash       the original hash
     * @param startIndex the startIndex used to create the sub hash
     * @param endIndex   the endIndex used to create the sub hash
     * @return a short hash
     */
    protected String computeShortenedHash(String hash, int startIndex, int endIndex) {
        var shortenedHash = hashService.createShortHash(hash, startIndex, endIndex);
        return urlRepository.findByShortenedHash(shortenedHash).map(_ -> {
            // If we tried all possibilities then it's time to throw an error
            if (endIndex == hash.length()) {
                log.error(STR. "No short hash available for the hash \{ hash }" );
                throw new NoShortenedUrlAvailableException("No shortened url can be generated for this url");
            }
            // If the short hash is already in the bdd then try to generate a new one
            return computeShortenedHash(hash, startIndex + 1, endIndex + 1);
        }).orElse(shortenedHash);
    }

}
