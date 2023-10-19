package com.doutoutdou.urlshortener.controller;

import com.doutoutdou.urlshortener.dto.ShortenedUrlRequestDto;
import com.doutoutdou.urlshortener.dto.ShortenedUrlResponseDto;
import com.doutoutdou.urlshortener.dto.UrlRequestDto;
import com.doutoutdou.urlshortener.dto.UrlResponseDto;
import com.doutoutdou.urlshortener.exception.ShortenedUrlNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.doutoutdou.urlshortener.utils.Constants.SHORTENED_URL_PARAMETER_MANDATORY;

@RequestMapping("/v1/url")
public interface UrlOperations {

    @GetMapping(produces = "application/json")
    @Operation(summary = "Get the original url from a shortened url")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Url found from the shortened url provided",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UrlResponseDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "ShortenedUrl parameter is invalid"),
            @ApiResponse(responseCode = "404", description = "Shortened url not found")})
    ResponseEntity<UrlResponseDto> getFromShortenedUrl(
            @Valid
            @NotNull(message = SHORTENED_URL_PARAMETER_MANDATORY)
            @RequestParam(name = "shortenedUrl", required = false)
            // Open api documentation
            @Parameter(required = true, content = @Content(
                    examples = @ExampleObject(value = "https://urlshortened/a6f883ddbe"),
                    schema = @Schema(implementation = ShortenedUrlRequestDto.class)))
            ShortenedUrlRequestDto shortenedUrl) throws ShortenedUrlNotFoundException;

    @PostMapping(produces = "application/json", consumes = "application/json")
    @Operation(summary = "Create a shortened url")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shortened url created",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShortenedUrlResponseDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid original url format supplied"),
            @ApiResponse(responseCode = "500", description = "An internal server error occurred")})
    ResponseEntity<ShortenedUrlResponseDto> createShortenedUrl(
            @Valid
            @NotNull(message = "Body is required")
            @org.springframework.web.bind.annotation.RequestBody(required = false)
            // Open api documentation
            @RequestBody(description = "Url to shorten", required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UrlRequestDto.class)))
            UrlRequestDto dto);
}
