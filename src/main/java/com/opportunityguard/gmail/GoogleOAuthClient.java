package com.opportunityguard.gmail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opportunityguard.config.Config;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Google OAuth 2.0 authorization-code flow over plain HTTPS. The user's Google password is never handled. */
public class GoogleOAuthClient {
    public static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public record Tokens(String accessToken, String refreshToken) {}

    public boolean isConfigured() {
        return Config.isSet("GOOGLE_CLIENT_ID") && Config.isSet("GOOGLE_CLIENT_SECRET");
    }

    public static String redirectUri() {
        return Config.get("GOOGLE_REDIRECT_URI", "http://localhost:8080/OpportunityGuardAI/gmail/callback");
    }

    public String authorizationUrl(String state) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("client_id", Config.get("GOOGLE_CLIENT_ID", ""));
        p.put("redirect_uri", redirectUri());
        p.put("response_type", "code");
        p.put("scope", SCOPE);
        p.put("access_type", "offline");
        p.put("prompt", "select_account consent");
        p.put("state", state);
        return "https://accounts.google.com/o/oauth2/v2/auth?" + form(p);
    }

    public Tokens exchangeCode(String code) throws IOException, InterruptedException {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("code", code);
        p.put("client_id", Config.get("GOOGLE_CLIENT_ID", ""));
        p.put("client_secret", Config.get("GOOGLE_CLIENT_SECRET", ""));
        p.put("redirect_uri", redirectUri());
        p.put("grant_type", "authorization_code");
        JsonObject o = post(p);
        String refresh = o.has("refresh_token") ? o.get("refresh_token").getAsString() : null;
        return new Tokens(o.get("access_token").getAsString(), refresh);
    }

    public String refreshAccessToken(String refreshToken) throws IOException, InterruptedException {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("refresh_token", refreshToken);
        p.put("client_id", Config.get("GOOGLE_CLIENT_ID", ""));
        p.put("client_secret", Config.get("GOOGLE_CLIENT_SECRET", ""));
        p.put("grant_type", "refresh_token");
        return post(p).get("access_token").getAsString();
    }

    private JsonObject post(Map<String, String> params) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(params)))
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject o;
        try {
            o = JsonParser.parseString(resp.body()).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IOException("Google token endpoint returned an unreadable response (HTTP " + resp.statusCode() + ")");
        }
        if (resp.statusCode() != 200 || !o.has("access_token")) {
            String detail = o.has("error_description") ? o.get("error_description").getAsString()
                    : o.has("error") ? o.get("error").getAsString() : "unknown error";
            throw new IOException("Google token endpoint returned HTTP " + resp.statusCode() + ": " + detail);
        }
        return o;
    }

    private static String form(Map<String, String> p) {
        return p.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
