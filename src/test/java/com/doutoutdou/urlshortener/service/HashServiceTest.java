package com.doutoutdou.urlshortener.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class HashServiceTest {

    @InjectMocks
    private HashService hashService;

    @ParameterizedTest(name = "Create hash from {0} should return a hash with a size of 64")
    @MethodSource("urls")
    public void createHash(String input) {
        var hash = hashService.createHash(input);
        Assertions.assertThat(hash.length()).isEqualTo(64);
    }

    public Stream<Arguments> urls() {
        return Stream.of(
                Arguments.of(
                        "https://www.url1.com/test"
                ),
                Arguments.of(
                        "http://sample.info/?insect=fireman&porter=attraction#cave"
                ),
                Arguments.of(
                        "https://example.com/digitalocean-mercurial-social-media-email-marketing-netlify/P"
                )
        );
    }

    @ParameterizedTest(name = "Create a short hash from {0} with start index {1} and end index {2} should return {3}")
    @MethodSource("hashs")
    public void createShortHash(String hash, int startIndex, int endIndex, String expectedOutputHash) {
        var shortHash = hashService.createShortHash(hash, startIndex, endIndex);
        Assertions.assertThat(shortHash.length()).isEqualTo(endIndex - startIndex);
        Assertions.assertThat(shortHash).isEqualTo(expectedOutputHash);
    }

    public Stream<Arguments> hashs() {
        return Stream.of(
                Arguments.of(
                        "fa2908526bbc132d31ba7dfa027c9fcf463fd64a59fc83d6af2ad21942ac0d64",
                        0, 10, "fa2908526b"
                ),
                Arguments.of(
                        "c36ca1d22eed92fda2ad5f1d27df07b2e5d05fae55a8f7c778ec45044ba55ad4",
                        7, 11, "22ee"
                ),
                Arguments.of(
                        "a09205343f990dbcaf88878ff8c22a62a1894614bc814ed1ad3ab78a53518252",
                        17, 46, "f88878ff8c22a62a1894614bc814e"
                )
        );
    }

}
