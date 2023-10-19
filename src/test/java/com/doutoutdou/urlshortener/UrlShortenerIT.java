package com.doutoutdou.urlshortener;

import com.doutoutdou.urlshortener.dto.ShortenedUrlResponseDto;
import com.doutoutdou.urlshortener.dto.UrlRequestDto;
import com.doutoutdou.urlshortener.dto.UrlResponseDto;
import com.doutoutdou.urlshortener.entity.Url;
import com.doutoutdou.urlshortener.repository.UrlRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.doutoutdou.urlshortener.utils.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UrlShortenerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
public class UrlShortenerIT {

    @Autowired
    UrlRepository urlRepository;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    private String buildUrl() {
        return "http://localhost:" + port + "/api/v1/url";
    }

    @BeforeEach
    void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "url");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Create method")
    class Create {


        /**
         * Call the endpoint for creation and return the response
         * fail is the status is not 200 or if the response is null or not respecting the format expected
         *
         * @param url the url to process
         * @return the shortened url created
         */
        private ShortenedUrlResponseDto createAndReturnResponse(String url) {

            ResponseEntity<ShortenedUrlResponseDto> response = restTemplate.postForEntity(buildUrl(), new UrlRequestDto(url), ShortenedUrlResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getShortenedUrl()).matches(SHORTENED_URL_PATTERN);

            return response.getBody();
        }


        @DisplayName("Create should save in database and then return a shortened url")
        @Test
        public void create() {
            var originalUrl = "http://localhost:8080/api/swagger-ui/index.html";

            var shortenedUrl = createAndReturnResponse(originalUrl).getShortenedUrl();

            var shortHash = shortenedUrl.substring(SHORTENED_URL_BASE_PATH.length());

            // verify that data are saved in database
            Optional<Url> url = urlRepository.findByShortenedHash(shortHash);
            url.ifPresentOrElse(value ->
            {
                // should be encoded in database due to encoding
                assertThat(value.getOriginUrl()).startsWith("http%3A%2F%2F");
                assertThat(value.getOriginUrl()).isEqualTo(URLEncoder.encode(originalUrl, StandardCharsets.UTF_8));
                // when decoded, values must be equal
                assertThat(URLDecoder.decode(value.getOriginUrl(), StandardCharsets.UTF_8)).isEqualTo(originalUrl);
                assertThat(value.getHash()).contains(shortHash);
            }, () -> fail("Url not found in database"));
        }

        @DisplayName("Create with same url twice should return the same shortened url")
        @Test
        public void createTwice() {
            var url = "http://localhost:8080/api/swagger-ui/index.html";

            var firstShortenedUrl = createAndReturnResponse(url).getShortenedUrl();

            var secondShortenedUrl = createAndReturnResponse(url).getShortenedUrl();

            assertThat(secondShortenedUrl).isEqualTo(firstShortenedUrl);

            // verify that data are saved only once in database
            assertThat(IteratorUtils.size(urlRepository.findAll().iterator())).isEqualTo(1);
        }

        @DisplayName("Create for different urls should return different shortenedUrl")
        @Test
        public void createForMultipleUrls() {
            var firstShortenedUrl = createAndReturnResponse("http://localhost:8080/api/v1/url").getShortenedUrl();
            var secondShortenedUrl = createAndReturnResponse("http://localhost:8080/api/v2/url").getShortenedUrl();
            var thirdShortenedUrl = createAndReturnResponse("http://localhost:8080/api/v3/url").getShortenedUrl();

            assertThat(firstShortenedUrl).isNotEqualTo(secondShortenedUrl);
            assertThat(firstShortenedUrl).isNotEqualTo(thirdShortenedUrl);
            assertThat(secondShortenedUrl).isNotEqualTo(thirdShortenedUrl);

            // verify that data are all saved in database
            assertThat(IteratorUtils.size(urlRepository.findAll().iterator())).isEqualTo(3);
        }


        @DisplayName("Create an url with special characters should return a shortened url")
        @Test
        public void createWithSpecialCharacters() {
            createAndReturnResponse("http://localhost:8080/api/swagger-ui/index.html?param=sdfasfa!@#$%^&*()_+ :'.,/{[}]|\\");
        }

        @DisplayName("Create an url with max size should return a shortened url")
        @Test
        public void createWithSpecialCharactersAndWithMaxSize() {
            // Generate a random string and add it to the url
            var baseUrl = "http://localhost:8080/api/swagger-ui/index.html?parameters=";
            var randomParameters = RandomStringUtils.random(2048 - baseUrl.length(), true, true);

            var url = baseUrl + randomParameters;
            assertThat(url.length()).isEqualTo(2048);

            createAndReturnResponse(url);
        }


        @DisplayName("Create with invalid original url should return 400")
        @ParameterizedTest(name = "Create with original url equals to {0}  should return a bad request")
        @MethodSource("wrongOriginalUrl")
        public void createWithWrongOriginalUrl(UrlRequestDto urlRequestDto, List<String> errorMessagesExpected) {
            ResponseEntity<String> response = restTemplate.postForEntity(buildUrl(), urlRequestDto, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            errorMessagesExpected.forEach(
                    s -> assertThat(response.getBody()).contains(s)
            );
        }

        public Stream<Arguments> wrongOriginalUrl() {
            return Stream.of(
                    Arguments.of(new UrlRequestDto(), List.of(URL_IS_REQUIRED)),
                    Arguments.of(new UrlRequestDto("wrong"), List.of(URL_LENGTH, URL_PATTERN)),
                    Arguments.of(new UrlRequestDto("thisisalongandwrongformaturl"), List.of(URL_PATTERN)),
                    Arguments.of(new UrlRequestDto("http://short"), List.of(URL_LENGTH)),
                    // Generating an url of 2049 must fail
                    Arguments.of(new UrlRequestDto("http://" + RandomStringUtils.random(2042, true, true)), List.of(URL_LENGTH))

            );
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Get method")
    class Get {
        @DisplayName("Get with a known shortenedUrl should return the original url")
        @Test
        public void getWithKnownShortenedUrl() {
            var originalUrl = "http://localhost:8080/api/swagger-ui/index.html?param=sdfasfa!@#$%^&*()_+ :'.,/{[}]|\\";
            var urlRequestDto = new UrlRequestDto(originalUrl);

            ResponseEntity<ShortenedUrlResponseDto> response = restTemplate.postForEntity(buildUrl(), urlRequestDto, ShortenedUrlResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            var shortenedUrl = response.getBody().getShortenedUrl();
            assertThat(response.getBody().getShortenedUrl()).matches(SHORTENED_URL_PATTERN);

            ResponseEntity<UrlResponseDto> getResponse = restTemplate.getForEntity(buildUrl() + STR. "?shortenedUrl=\{ shortenedUrl }" , UrlResponseDto.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody().getUrl()).isEqualTo(originalUrl);

        }

        @DisplayName("Get with unknown shortenedUrl should return 404")
        @Test
        public void getWithUnknownShortenedUrl() {
            ResponseEntity<String> response = restTemplate.getForEntity(buildUrl() + "?shortenedUrl=https://urlshortened/1234567890", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).contains("Shortened url not found");
        }

        @DisplayName("Get with invalid shortenedUrl should return 400")
        @ParameterizedTest(name = "Get with shortenedUrl equals to {0} should return a bad request")
        @MethodSource("wrongShortenedUrl")
        public void getWithWrongShortenedUrlParameter(String shortenedUrl, String errorMessageExpected) {
            ResponseEntity<String> response = restTemplate.getForEntity(buildUrl() + shortenedUrl, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains(errorMessageExpected);
        }

        public Stream<Arguments> wrongShortenedUrl() {
            return Stream.of(
                    Arguments.of("", SHORTENED_URL_PARAMETER_MANDATORY),
                    Arguments.of("?rers", SHORTENED_URL_PARAMETER_MANDATORY),
                    Arguments.of("?shortened", SHORTENED_URL_PARAMETER_MANDATORY),
                    Arguments.of("?shortenedUrl", SHORTENED_URL_PARAMETER_FORMAT),
                    Arguments.of("?shortenedUrl=https://urlshortened/12345678901", SHORTENED_URL_PARAMETER_FORMAT),
                    Arguments.of("?shortenedUrl=https://urlshortened/123456789*", SHORTENED_URL_PARAMETER_FORMAT),
                    Arguments.of("?shortenedUrl=https://urlshortened/1234567", SHORTENED_URL_PARAMETER_FORMAT),
                    Arguments.of("?shortenedUrl=https://urlshortened/12345678901", SHORTENED_URL_PARAMETER_FORMAT),
                    Arguments.of("?shortenedUrl=https://urlshoened/1234567890", SHORTENED_URL_PARAMETER_FORMAT)
            );
        }
    }

}

