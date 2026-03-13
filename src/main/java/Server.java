import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute: mvn -q -DskipTests clean compile exec:java
 */
public class Server {
    private static final int PORT = 8000;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/proxy", new ProxyHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        System.out.println("Serving on http://localhost:" + PORT);
        server.start();
    }

    static class StaticHandler implements HttpHandler {
        private final Path baseDir = Paths.get("").toAbsolutePath()
                .resolve("src/main/resources/static");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String rawPath = exchange.getRequestURI().getPath();
            if (rawPath.equals("/")) rawPath = "/index.html";

            Path filePath = baseDir.resolve(rawPath.substring(1)).normalize();
            if (!filePath.startsWith(baseDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String contentType = guessContentType(filePath);
            byte[] data = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, "HEAD".equalsIgnoreCase(method) ? -1 : data.length);
            if (!"HEAD".equalsIgnoreCase(method)) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            }
        }
    }

    static class ProxyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange.getRequestBody());
            Map<String, Object> payload;
            try {
                payload = asMap(GSON.fromJson(body, Map.class));
            } catch (RuntimeException ex) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid JSON payload.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            String url = stringValue(payload.get("url"));
            if (url == null || url.isBlank()) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Missing url.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            String method = stringValue(payload.get("method"));
            if (method == null || method.isBlank()) method = "GET";

            String requestBody = stringValue(payload.get("body"));
            Number timeoutVal = numberValue(payload.get("timeout"));
            int timeoutMs = timeoutVal == null ? 0 : Math.max(0, timeoutVal.intValue());

            Map<String, Object> headers = asMap(payload.get("headers"));

            long start = Instant.now().toEpochMilli();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod(method);
                if (timeoutMs > 0) {
                    conn.setConnectTimeout(timeoutMs);
                    conn.setReadTimeout(timeoutMs);
                }
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), stringValue(entry.getValue()));
                }

                if (requestBody != null && !requestBody.isBlank()
                        && !method.equalsIgnoreCase("GET")
                        && !method.equalsIgnoreCase("HEAD")) {
                    conn.setDoOutput(true);
                    byte[] out = requestBody.getBytes(StandardCharsets.UTF_8);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(out);
                    }
                }

                int status = conn.getResponseCode();
                String statusText = conn.getResponseMessage();
                String contentType = conn.getHeaderField("content-type");
                InputStream responseStream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
                String responseBody = responseStream == null ? "" : readBody(responseStream);
                long durationMs = Instant.now().toEpochMilli() - start;

                JsonObject result = new JsonObject();
                result.addProperty("status", status);
                result.addProperty("statusText", statusText == null ? "" : statusText);
                JsonObject headerObj = new JsonObject();
                headerObj.addProperty("content-type", contentType == null ? "" : contentType);
                result.add("headers", headerObj);
                result.addProperty("body", responseBody);
                result.addProperty("durationMs", durationMs);
                sendJson(exchange, 200, GSON.toJson(result));
            } catch (Exception ex) {
                JsonObject error = new JsonObject();
                error.addProperty("error", ex.getMessage() == null ? "Request failed." : ex.getMessage());
                sendJson(exchange, 502, error.toString());
            }
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Content-Length", String.valueOf(data.length));
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static String readBody(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = stream.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static String guessContentType(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map) return (Map<String, Object>) value;
        return new HashMap<>();
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Number numberValue(Object value) {
        return value instanceof Number ? (Number) value : null;
    }
}
