package com.example.liquidbase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthService {

    @Value("${oauth2.auth-server-url}")
    private String authServerUrl;

    @Value("${oauth2.client-id}")
    private String clientId;

    @Value("${oauth2.client-secret}")
    private String clientSecret;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tạo URL đăng nhập
     */
    public String buildLoginUrl(String state) {
        return authServerUrl + "/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&state=" + state;
    }

    /**
     * Đổi Code lấy Token
     */
    public Map<String, Object> exchangeCodeForToken(String code) throws Exception {
        String url = authServerUrl + "/oauth/token";

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("redirect_uri", redirectUri);

        log.info("[OAuth2] Sending POST request to exchange code for token at: {}", url);
        String responseJson = restTemplate.postForObject(url, body, String.class);
        log.debug("[OAuth2] Token response: {}", responseJson);
        
        JsonNode node = mapper.readTree(responseJson);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access_token", node.get("access_token").asText());
        tokens.put("refresh_token", node.get("refresh_token").asText());
        log.info("[OAuth2] Successfully retrieved access token.");
        return tokens;
    }

    /**
     * Lấy thông tin User
     */
    public JsonNode getUserInfo(String accessToken) throws Exception {
        String url = authServerUrl + "/oauth/userinfo";

        // Tạo header Authorization
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        log.info("[OAuth2] Fetching user info from: {}", url);
        String responseJson = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class).getBody();
        log.debug("[OAuth2] User info response: {}", responseJson);
        
        return mapper.readTree(responseJson);
    }
}
