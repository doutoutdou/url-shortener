package com.doutoutdou.urlshortener.service;

import com.doutoutdou.urlshortener.dto.ShortenedUrlRequestDto;
import com.doutoutdou.urlshortener.dto.UrlRequestDto;
import com.doutoutdou.urlshortener.entity.Url;
import com.doutoutdou.urlshortener.exception.NoShortenedUrlAvailableException;
import com.doutoutdou.urlshortener.exception.ShortenedUrlNotFoundException;
import com.doutoutdou.urlshortener.repository.UrlRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private HashService hashService;

    @InjectMocks
    private UrlService urlService;

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Find method")
    class FindMethod {
        @Test
        @DisplayName("Find from shortened url should return original url if known")
        public void findFromShortenedUrlShouldReturnOriginalUrl() throws ShortenedUrlNotFoundException {
            var url = Url.builder().originUrl("originalUrl").build();
            var shortenedUrlDto = new ShortenedUrlRequestDto("https://urlshortened/a2321fafe4");

            when(urlRepository.findByShortenedHash("a2321fafe4")).thenReturn(Optional.of(url));

            var urlResponseDto = urlService.findFromShortenedUrl(shortenedUrlDto);

            Assertions.assertThat(urlResponseDto.getUrl()).isEqualTo(url.getOriginUrl());
        }

        @Test
        @DisplayName("Find from shortened url should throw exception if not found")
        public void findFromShortenedUrlShouldThrowException() {
            var shortenedUrlDto = new ShortenedUrlRequestDto("https://urlshortened/a2321fafe4");

            when(urlRepository.findByShortenedHash("a2321fafe4")).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> urlService.findFromShortenedUrl(shortenedUrlDto))
                    .isInstanceOf(ShortenedUrlNotFoundException.class)
                    .hasMessageContaining("Shortened url not found");

        }

    }

    @Nested
    @DisplayName("Build method")
    class BuildMethod {
        private final String originUrl = "https://www.myawesomeurl/321";
        private final String hash = "fa2908526bbc132d31b";
        private final String shortHash = "ab";
        private final String shortenedUrl = "https://urlshortened/ab";

        @DisplayName("buildShortenedUrl should return directly if already existing")
        @Test
        public void buildShortenedUrlForUrlAlreadyKnown() {
            var url = Url.builder()
                    .originUrl(originUrl)
                    .hash(hash)
                    .shortenedHash(shortHash)
                    .build();

            when(hashService.createHash(originUrl)).thenReturn(hash);

            when(urlRepository.findByHash(hash)).thenReturn(Optional.of(url));

            var shortenedUrlResponseDto = urlService.buildShortenedUrl(new UrlRequestDto(originUrl));

            assertThat(shortenedUrlResponseDto.getShortenedUrl()).isEqualTo(shortenedUrl);

            verify(urlRepository, times(1)).findByHash(hash);
            verify(urlRepository, never()).save(any());
            verify(urlRepository, never()).findByShortenedHash(anyString());
        }

        @DisplayName("buildShortenedUrl should create and save in database if unknown")
        @Test
        public void buildShortenedUrlForUrlUnknown() {
            var url = Url.builder()
                    .originUrl(originUrl)
                    .hash(hash)
                    .shortenedHash(shortHash)
                    .build();

            when(hashService.createHash(originUrl)).thenReturn(hash);
            when(hashService.createShortHash(anyString(), anyInt(), anyInt())).thenReturn(shortHash);

            when(urlRepository.findByHash(hash)).thenReturn(Optional.empty());
            when(urlRepository.findByShortenedHash("ab")).thenReturn(Optional.empty());

            // TODO : voir pk il ne capte pas avec l'url
            when(urlRepository.save(any(Url.class))).thenReturn(url);

            var shortenedUrlResponseDto = urlService.buildShortenedUrl(new UrlRequestDto(originUrl));

            assertThat(shortenedUrlResponseDto.getShortenedUrl()).isEqualTo(shortenedUrl);
        }

        @DisplayName("computeShortenedHash should return a short hash")
        @Test
        public void computeShortenedHashWithUrlNotFound() {
            // mock returning a value and then noting
            // this way we can test the recursive method
            when(urlRepository.findByShortenedHash(anyString()))
                    .thenReturn(Optional.of(new Url()))
                    .thenReturn(Optional.empty());
            when(hashService.createShortHash(anyString(), anyInt(), anyInt())).thenReturn("bcd").thenReturn("cde");

            var shortenedHash = urlService.computeShortenedHash("abcde", 1, 4);
            assertThat(shortenedHash).isEqualTo("cde");
        }

        @DisplayName("computeShortenedHash should throw an exception if no value available")
        @Test
        public void computeShortenedHashShouldThrowException() {
            when(urlRepository.findByShortenedHash(anyString())).thenReturn(Optional.of(new Url()));
            when(hashService.createShortHash(anyString(), anyInt(), anyInt())).thenReturn("bcd");

            assertThatThrownBy(() -> urlService.computeShortenedHash("abcd", 1, 4))
                    .isInstanceOf(NoShortenedUrlAvailableException.class)
                    .hasMessageContaining("No shortened url can be generated for this url");
        }
    }


}
