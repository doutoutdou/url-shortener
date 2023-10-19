package com.doutoutdou.urlshortener.entity;


import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "url")
public class Url {

    @Column(name = "origin_url", length = 2048, nullable = false, unique = true)
    private String originUrl;

    @Column(name = "hash", length = 50, nullable = false, unique = true)
    private String hash;

    @Column(name = "shortened_hash", length = 50, nullable = false, unique = true)
    private String shortenedHash;

    @Id
    @SequenceGenerator(name = "URL_SEQ", sequenceName = "URL_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "URL_SEQ")
    private Long id;

}
