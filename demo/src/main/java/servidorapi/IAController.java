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
// Esta es la URL que coincide con tu lista de modelos disponibles
String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
        
        HttpClient client = HttpClient.newHttpClient();

// Limpiamos el texto para que no rompa el JSON
String promptLimpio = prompt.replace("\n", " ").replace("\"", "\\\"");

// Estructura oficial de Google
String body = "{ \"contents\": [{ \"parts\":[{ \"text\": \"" + promptLimpio + "\" }] }] }";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    } catch (Exception e) {
        return "{\"error\": \"" + e.getMessage() + "\"}";
    }
}
}