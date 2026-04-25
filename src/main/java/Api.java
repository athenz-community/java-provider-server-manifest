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
    static final String RESOURCE_NAME = "api:docs";

    static Authorizer authorizer;

    public static void main(String[] args) throws Exception {
        authorizer = new Authorizer();

        docs.put(new JSONObject().put("id", 1).put("name", "first doc").put("content", "hello world"));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/docs", exchange -> {
            try {
                authorizer.authorizeRequest(exchange, RESOURCE_NAME);

                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method)) {
                    sendResponse(exchange, 200, new JSONObject().put("docs", docs).toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    InputStream is = exchange.getRequestBody();
                    JSONObject newDoc = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    newDoc.put("id", docs.length() + 1);
                    docs.put(newDoc);
                    sendResponse(exchange, 201, new JSONObject().put("success", true).put("doc", newDoc).toString());
                } else {
                    sendResponse(exchange, 405, "Method Not Allowed");
                }

            } catch (IllegalArgumentException e) { // if 401
                JSONObject err = new JSONObject()
                        .put("status", 401)
                        .put("error", "Unauthorized")
                        .put("message", e.getMessage());
                try {
                    sendResponse(exchange, 401, err.toString());
                } catch (Exception ex) {
                }

            } catch (SecurityException e) { // if 403
                JSONObject err = new JSONObject()
                        .put("status", 403)
                        .put("error", "Forbidden")
                        .put("message", "Access denied by Athenz ZPE.")
                        .put("details", e.getMessage());
                try {
                    sendResponse(exchange, 403, err.toString());
                } catch (Exception ex) {
                }

            } catch (Throwable e) { // if 500
                System.err.println("\n🔥 [ERROR] Something went wrong during the API request processing.");
                e.printStackTrace();
                try {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
                } catch (IOException ex) {
                }
            }
        });

        server.start();
        System.out.println("🚀 Server started on port " + PORT + " (Athenz Required: " + authorizer.isRequired() + ")");
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