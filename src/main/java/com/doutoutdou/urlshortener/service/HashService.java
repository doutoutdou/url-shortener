package com.doutoutdou.urlshortener.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class HashService {

    /**
     * Create a hash using sha256 algorithm
     * @param input the input to hash
     * @return the SHA256 hash
     */
    public String createHash(String input) {
        return DigestUtils.sha256Hex(input);
    }

    /**
     * Create a short hash using substring method
     * @param hash the original hash
     * @param startIndex the startIndex for substring
     * @param endIndex the endIndex for substring
     * @return the short hash
     */
    public String createShortHash(String hash, int startIndex, int endIndex) {
        return hash.substring(startIndex, endIndex);
    }
}
