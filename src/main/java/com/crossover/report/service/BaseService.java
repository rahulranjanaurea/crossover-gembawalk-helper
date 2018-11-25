package com.crossover.report.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public abstract class BaseService implements IService {

    private static String token;
    protected RestTemplate restTemplate = new RestTemplate();
    @Value("${base.url}")
    String baseUrl;
    @Value("${username}")
    String username;
    @Value("${password}")
    String password;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        BaseService.token = token;
        System.out.println("Token:"+token);
    }

    public abstract String getUrl();

    HttpHeaders getHeaders() {
        if (getToken() == null) {
            HttpHeaders headers = new HttpHeaders();
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + encodedAuth;
            headers.set("Authorization", authHeader);
            return headers;

        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", getToken());
            return headers;
        }
    }

    @Override
    public <T> T getEntity(Class<T> clazz, String... params) {
        System.out.println(getCompleteUrl(params));
        return restTemplate.exchange(getCompleteUrl(params), HttpMethod.GET, new HttpEntity<Object>(getHeaders()), clazz).getBody();
    }

    protected String getCompleteUrl(String... params) {
        String url = baseUrl + getUrl();
        if (params.length > 0) {
            url += "?" + Arrays.stream(params).collect(Collectors.joining("&"));
        }
        return url;
    }

    @Override
    public <T> T postEntity(Class<T> clazz, Object body, String... params) {
        return restTemplate.exchange(getCompleteUrl(params), HttpMethod.POST, new HttpEntity<Object>
                (body, getHeaders()), clazz).getBody();
    }
}
