import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Api {
    static JSONArray docs = new JSONArray();
    static int port = 14443;

    public static void main(String[] args) throws Exception {
        // Initial data in memory
        JSONObject firstDoc = new JSONObject();
        firstDoc.put("id", 1);
        firstDoc.put("name", "first doc");
        firstDoc.put("content", "hello world");
        docs.put(firstDoc);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/api/docs", exchange -> {
            String method = exchange.getRequestMethod()
;
            
            if ("GET".equalsIgnoreCase(method)) {
                JSONObject res = new JSONObject();
                res.put("docs", docs);
                sendResponse(exchange, 200, res.toString());
                
            } else if ("POST".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                
                JSONObject newDoc = new JSONObject(body);
                newDoc.put("id", docs.length() + 1);
                docs.put(newDoc);
                
                JSONObject res = new JSONObject();
                res.put("success", true);
                res.put("message", "Successfully added the doc!");
                res.put("doc", newDoc);
                
                sendResponse(exchange, 201, res.toString());
                
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        });
        
        server.setExecutor(null);
        server.start();
        System.out.println("Java Dummy Server running on port " + port);
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