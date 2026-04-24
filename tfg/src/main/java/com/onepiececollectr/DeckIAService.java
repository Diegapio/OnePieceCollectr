package com.onepiececollectr;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DeckIAService {
    private static final String RENDER_URL = "https://onepiececollectr.onrender.com/api/mazo";

    public CompletableFuture<String> llamarIA(String prompt) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RENDER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(prompt))
                .build();

        // Lo hacemos asíncrono para que la App no se congele mientras espera
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}