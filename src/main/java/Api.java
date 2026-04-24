import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Api {
    static JSONArray docs = new JSONArray();

    static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "14443"));
    static final boolean AT_REQUIRED = Boolean.parseBoolean(System.getenv().getOrDefault("AT_REQUIRED", "true"));
    static final String RESOURCE_NAME = "shopping.api:docs";
    static final String DEFAULT_JWK_URI = "http://localhost:8443/zts/v1/jwk";

    static Authorizer authorizer;

    public static void main(String[] args) throws Exception {
        if (AT_REQUIRED) {
            if (System.getProperty("athenz.zpe.jwk_uri") == null) {
                System.setProperty("athenz.zpe.jwk_uri", DEFAULT_JWK_URI);
            }

            authorizer = new Authorizer();
        }

        // default data:
        docs.put(new JSONObject().put("id", 1).put("name", "first doc").put("content", "hello world"));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/docs", exchange -> {
            String method = exchange.getRequestMethod();

            // TODO: Apply authorization logic inside the each method, rather than here.
            // TODO: Add more proper HTTP methods
            if (AT_REQUIRED) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                String token = (authHeader != null && authHeader.startsWith("Bearer "))
                        ? authHeader.substring(7)
                        : null;

                if (token == null) {
                    sendResponse(exchange, 401, "{\"error\": \"Unauthorized: Missing token\"}");
                    return;
                }

                String action = "GET".equalsIgnoreCase(method) ? "read" : "write";

                if (!authorizer.isAuthorized(action, RESOURCE_NAME, token)) {
                    sendResponse(exchange, 403, "{\"error\": \"Forbidden by Athenz ZPE\"}");
                    return;
                }
            }

            // --- Business Logic ---
            switch (method) {
                case "GET":
                    sendResponse(exchange, 200, new JSONObject().put("docs", docs).toString());
                    break;
                case "POST":
                    InputStream is = exchange.getRequestBody();
                    JSONObject newDoc = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    newDoc.put("id", docs.length() + 1);
                    docs.put(newDoc);
                    sendResponse(exchange, 201, new JSONObject().put("success", true).put("doc", newDoc).toString());
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
                    break;
            }
        });

        server.start();
        System.out.println("🚀 Server started on port " + PORT + " (Athenz Required: " + AT_REQUIRED + ")");
    }

    private static void sendResponse(HttpExchange exchange, int code, String res) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = res.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}