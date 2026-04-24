package servidorapi;

import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.http.*;

@RestController
@RequestMapping("/api")
public class IAController {

    private final String API_KEY = System.getenv("GEMINI_API_KEY");

    @PostMapping("/mazo")
    public String generarMazo(@RequestBody String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + API_KEY;
            HttpClient client = HttpClient.newHttpClient();
            
            String body = "{ \"contents\": [{ \"parts\":[{ \"text\": \"" + prompt + "\" }] }] }";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}