package com.opportunityguard.gmail;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Minimal Gmail REST client (read-only scope). */
public class GmailApiClient {
    private static final String BASE = "https://gmail.googleapis.com/gmail/v1/users/me/messages";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public List<String> listMessageIds(String accessToken, String query, int max) throws IOException, InterruptedException {
        URI uri = URI.create(BASE + "?maxResults=" + max + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        JsonObject o = getJson(uri, accessToken);
        List<String> ids = new ArrayList<>();
        if (o.has("messages")) {
            for (JsonElement e : o.getAsJsonArray("messages")) {
                ids.add(e.getAsJsonObject().get("id").getAsString());
            }
        }
        return ids;
    }

    public GmailMessage getMessage(String accessToken, String id) throws IOException, InterruptedException {
        JsonObject o = getJson(URI.create(BASE + "/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "?format=full"), accessToken);
        JsonObject payload = o.has("payload") ? o.getAsJsonObject("payload") : new JsonObject();
        String from = "";
        String subject = "";
        if (payload.has("headers")) {
            for (JsonElement h : payload.getAsJsonArray("headers")) {
                JsonObject ho = h.getAsJsonObject();
                String name = ho.get("name").getAsString();
                if ("From".equalsIgnoreCase(name)) from = ho.get("value").getAsString();
                else if ("Subject".equalsIgnoreCase(name)) subject = ho.get("value").getAsString();
            }
        }
        StringBuilder plain = new StringBuilder();
        StringBuilder html = new StringBuilder();
        collect(payload, plain, html);
        String body = plain.length() > 0 ? plain.toString().trim() : stripHtml(html.toString());
        if (body.isBlank() && o.has("snippet")) body = o.get("snippet").getAsString();
        return new GmailMessage(id, from, subject, body);
    }

    private void collect(JsonObject part, StringBuilder plain, StringBuilder html) {
        String mime = part.has("mimeType") ? part.get("mimeType").getAsString() : "";
        if (part.has("body") && part.getAsJsonObject("body").has("data")) {
            String data = part.getAsJsonObject("body").get("data").getAsString();
            String text = new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8);
            if (mime.startsWith("text/plain")) plain.append(text).append('\n');
            else if (mime.startsWith("text/html")) html.append(text).append('\n');
        }
        if (part.has("parts")) {
            JsonArray parts = part.getAsJsonArray("parts");
            for (JsonElement p : parts) collect(p.getAsJsonObject(), plain, html);
        }
    }

    private static String stripHtml(String html) {
        return html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?s)<[^>]+>", " ")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replaceAll("\\s+", " ").trim();
    }

    private JsonObject getJson(URI uri, String accessToken) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + accessToken).GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            String detail = "";
            try {
                JsonObject err = JsonParser.parseString(resp.body()).getAsJsonObject().getAsJsonObject("error");
                detail = err.get("message").getAsString();
            } catch (RuntimeException ignored) {
                // body was not the usual Google error JSON
            }
            throw new IOException("Gmail API returned HTTP " + resp.statusCode() + (detail.isEmpty() ? "" : ": " + detail));
        }
        return JsonParser.parseString(resp.body()).getAsJsonObject();
    }
}
