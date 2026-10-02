package com.opportunityguard.gemini;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opportunityguard.config.Config;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Calls the Gemini generateContent REST endpoint and returns the model's JSON text. */
public class GeminiClient {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public boolean isConfigured() {
        return Config.isSet("GEMINI_API_KEY");
    }

    public String generateJson(String prompt) throws IOException, InterruptedException {
        if (!isConfigured()) throw new IOException("GEMINI_API_KEY is not configured.");
        String model = Config.get("GEMINI_MODEL", "gemini-3.5-flash-lite");
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject gen = new JsonObject();
        gen.addProperty("temperature", 0);
        gen.addProperty("responseMimeType", "application/json");
        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", gen);

        HttpRequest req = HttpRequest.newBuilder(
                        URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", Config.get("GEMINI_API_KEY", ""))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> resp = null;

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 503 || attempt == maxRetries) {
                break;
            }

            long waitTime = (long) Math.pow(2, attempt) * 5000;
            Thread.sleep(waitTime);
        }
        if (resp.statusCode() != 200) {
            String detail = "";
            try {
                detail = JsonParser.parseString(resp.body()).getAsJsonObject().getAsJsonObject("error").get("message").getAsString();
            } catch (RuntimeException ignored) {
                // not the usual error JSON
            }
            throw new IOException("Gemini API returned HTTP " + resp.statusCode() + (detail.isEmpty() ? "" : ": " + detail));
        }
        try {
            return JsonParser.parseString(resp.body()).getAsJsonObject()
                    .getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (RuntimeException e) {
            throw new IOException("Gemini returned no usable text (the response may have been blocked).");
        }
    }
}
