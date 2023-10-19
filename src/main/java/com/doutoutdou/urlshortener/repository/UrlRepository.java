package com.doutoutdou.urlshortener.repository;

import com.doutoutdou.urlshortener.entity.Url;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UrlRepository extends CrudRepository<Url, Long> {
    Optional<Url> findByHash(String hash);
    Optional<Url> findByShortenedHash(String shortenedHash);

}
