package com.kovu.platform.http;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    static HttpMethod parse(String raw) {
        try {
            return HttpMethod.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // método HTTP que no soportamos (HEAD, OPTIONS, etc.)
        }
    }
}
