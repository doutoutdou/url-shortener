package com.doutoutdou.urlshortener.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public static final String SHORTENED_URL_BASE_PATH = "https://urlshortened/";
    public static final String SHORTENED_URL_PARAMETER_MANDATORY = "shortenedUrl parameter required";
    public static final String SHORTENED_URL_PARAMETER_FORMAT = "Invalid parameter shortenedUrl. It must start with " + SHORTENED_URL_BASE_PATH + " and then contains 10 alphanumeric characters";
    public static final String SHORTENED_URL_PATTERN = "^https://urlshortened/[a-z0-9]{10}$";
    public static final String URL_IS_REQUIRED = "url is required";
    public static final String URL_LENGTH = "The url length must between 15 and 2048 characters";
    public static final String URL_PATTERN = "The url must start with http:// or https://";


}
