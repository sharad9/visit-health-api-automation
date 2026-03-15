package com.visit.gwtapiflowbuilder.server;

import com.google.gson.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class FlowServer {
    private static final int PORT = 8000;
    private static final Gson GSON = new Gson();
    private static final String
            FLOW_DIR_NAME = "flows";
    private static final String INDEX_FILE = "index.json";
    private static final String DEFAULT_FLOW_NAME = "default.flow.meta";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/flows/run", new FlowRunHandler());
        server.createContext("/api/flows", new FlowStorageHandler());
        server.createContext("/api/proxy", new ProxyHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        System.out.println("Serving on http://localhost:" + PORT);
        server.start();
    }

    private static Path resolveStaticDir() {
        Path project = Paths.get("").toAbsolutePath();
        Path built = project.resolve("target/gwt-api-flow-builder-1.0.0");
        if (Files.exists(built)) {
            return built;
        }
        Path webapp = project.resolve("src/main/webapp");
        if (Files.exists(webapp)) {
            return webapp;
        }
        return project;
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

    private static void addCors(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String formatHeaders(java.util.Map<String, java.util.List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null) continue;
            java.util.List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                sb.append(key).append(": ").append("\n");
            } else {
                for (String value : values) {
                    sb.append(key).append(": ").append(value).append("\n");
                }
            }
        }
        return sb.toString().trim();
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

    static class StaticHandler implements HttpHandler {
        private final Path baseDir = resolveStaticDir();

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

    static class FlowStorageHandler implements HttpHandler {
        private final Path baseDir = Paths.get("").toAbsolutePath().resolve(FLOW_DIR_NAME);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/save")) {
                handleSave(exchange);
                return;
            }
            if (path.endsWith("/list")) {
                handleList(exchange);
                return;
            }
            if (path.endsWith("/load")) {
                handleLoad(exchange);
                return;
            }
            exchange.sendResponseHeaders(404, -1);
        }

        private void handleSave(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = readBody(exchange.getRequestBody());
            JsonObject payload;
            try {
                payload = JsonParser.parseString(body).getAsJsonObject();
            } catch (RuntimeException ex) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid JSON payload.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            String name = payload.has("name") ? payload.get("name").getAsString() : DEFAULT_FLOW_NAME;
            String json = payload.has("json") ? payload.get("json").getAsString() : null;
            if (json == null || json.isBlank()) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Missing json.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            Files.createDirectories(baseDir);
            String safeName = sanitizeName(name);
            Path flowFile = baseDir.resolve(safeName + ".json");
            Files.writeString(flowFile, json, StandardCharsets.UTF_8);

            JsonObject entry = new JsonObject();
            entry.addProperty("name", safeName);
            entry.addProperty("file", flowFile.getFileName().toString());
            entry.addProperty("updatedAt", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            updateIndex(entry);

            JsonObject ok = new JsonObject();
            ok.addProperty("name", safeName);
            ok.addProperty("file", flowFile.getFileName().toString());
            sendJson(exchange, 200, ok.toString());
        }

        private void handleList(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Files.createDirectories(baseDir);
            Path indexPath = baseDir.resolve(INDEX_FILE);
            if (!Files.exists(indexPath)) {
                sendJson(exchange, 200, "[]");
                return;
            }
            String data = Files.readString(indexPath, StandardCharsets.UTF_8);
            sendJson(exchange, 200, data);
        }

        private void handleLoad(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Files.createDirectories(baseDir);
            String query = exchange.getRequestURI().getQuery();
            String name = DEFAULT_FLOW_NAME;
            if (query != null && query.startsWith("name=")) {
                name = query.substring("name=".length());
            }
            String safeName = sanitizeName(name);
            Path flowFile = baseDir.resolve(safeName + ".json");
            if (!Files.exists(flowFile)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String data = Files.readString(flowFile, StandardCharsets.UTF_8);
            sendJson(exchange, 200, data);
        }

        private void updateIndex(JsonObject entry) throws IOException {
            Path indexPath = baseDir.resolve(INDEX_FILE);
            JsonArray arr;
            if (Files.exists(indexPath)) {
                try {
                    arr = JsonParser.parseString(Files.readString(indexPath, StandardCharsets.UTF_8)).getAsJsonArray();
                } catch (RuntimeException ex) {
                    arr = new JsonArray();
                }
            } else {
                arr = new JsonArray();
            }

            String name = entry.get("name").getAsString();
            JsonObject match = null;
            for (JsonElement element : arr) {
                JsonObject obj = element.getAsJsonObject();
                if (name.equals(obj.get("name").getAsString())) {
                    match = obj;
                    break;
                }
            }
            if (match == null) {
                match = new JsonObject();
                arr.add(match);
            }
            match.addProperty("name", name);
            match.addProperty("file", entry.get("file").getAsString());
            match.addProperty("updatedAt", entry.get("updatedAt").getAsString());
            Files.writeString(indexPath, GSON.toJson(arr), StandardCharsets.UTF_8);
        }

        private String sanitizeName(String name) {
            if (name == null || name.isBlank()) {
                return DEFAULT_FLOW_NAME;
            }
            return name.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
    }

    static class ProxyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange.getRequestBody());
            JsonObject payload;
            try {
                payload = JsonParser.parseString(body).getAsJsonObject();
            } catch (RuntimeException ex) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid JSON payload.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            String method = payload.has("method") ? payload.get("method").getAsString() : "GET";
            String url = payload.has("url") ? payload.get("url").getAsString() : "";
            if (url == null || url.isBlank()) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Missing url.");
                sendJson(exchange, 400, error.toString());
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Only http/https URLs are allowed.");
                sendJson(exchange, 400, error.toString());
                return;
            }

            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL target = new java.net.URL(url);
                conn = (java.net.HttpURLConnection) target.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod(method.toUpperCase());

                if (payload.has("headers") && payload.get("headers").isJsonObject()) {
                    JsonObject headers = payload.getAsJsonObject("headers");
                    for (String key : headers.keySet()) {
                        if (key == null) continue;
                        String lower = key.toLowerCase();
                        if ("host".equals(lower) || "content-length".equals(lower)) {
                            continue;
                        }
                        String value = headers.get(key).getAsString();
                        conn.setRequestProperty(key, value);
                    }
                }

                String reqBody = payload.has("body") ? payload.get("body").getAsString() : null;
                boolean hasBody = reqBody != null && !reqBody.isEmpty();
                if (hasBody && !"GET".equalsIgnoreCase(method)) {
                    conn.setDoOutput(true);
                    byte[] bytes = reqBody.getBytes(StandardCharsets.UTF_8);
                    conn.getOutputStream().write(bytes);
                }

                int status = conn.getResponseCode();
                InputStream responseStream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
                String responseBody = responseStream == null ? "" : readBody(responseStream);
                String headers = formatHeaders(conn.getHeaderFields());

                JsonObject out = new JsonObject();
                out.addProperty("status", status);
                out.addProperty("headers", headers);
                out.addProperty("body", responseBody);
                sendJson(exchange, 200, out.toString());
            } catch (Exception ex) {
                JsonObject error = new JsonObject();
                error.addProperty("error", ex.getMessage() == null ? "Proxy error." : ex.getMessage());
                sendJson(exchange, 500, error.toString());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Flow Run Handler  —  GET /api/flows/run?name=<flowName>
    // -------------------------------------------------------------------------

    static class FlowRunHandler implements HttpHandler {
        private static final String FLOW_DIR = "flows";
        private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

        private static JsonArray buildStepsSummaryJson(java.util.List<FlowRunner.StepResult> steps) {
            JsonArray arr = new JsonArray();
            for (FlowRunner.StepResult s : steps) {
                JsonObject o = new JsonObject();
                o.addProperty("stepIdentifier", s.stepIdentifier);
                o.addProperty("method", s.method);
                o.addProperty("url", s.resolvedUrl);
                o.addProperty("statusCode", s.statusCode);
                o.addProperty("durationMs", s.durationMs);
                o.addProperty("passed", s.passed);
                if (s.errorMessage != null) o.addProperty("error", s.errorMessage);
                arr.add(o);
            }
            return arr;
        }

        private static String buildHtmlReport(FlowRunner.RunReport report) {
            String overallStatus = report.failedSteps == 0 ? "PASS" : "FAIL";
            String statusColor = report.failedSteps == 0 ? "#16a34a" : "#dc2626";
            String statusBg = report.failedSteps == 0 ? "#dcfce7" : "#fee2e2";

            StringBuilder stepsHtml = new StringBuilder();
            int idx = 1;
            for (FlowRunner.StepResult s : report.steps) {
                String sc = s.passed ? "#16a34a" : "#dc2626";
                String sbg = s.passed ? "#dcfce7" : "#fee2e2";
                String pill = "<span style=\"background:" + sbg + ";color:" + sc
                        + ";padding:2px 10px;border-radius:99px;font-size:11px;font-weight:700;\">"
                        + (s.passed ? "PASS" : "FAIL") + "</span>";

                StringBuilder checksHtml = new StringBuilder();
                for (FlowRunner.CheckResult c : s.checks) {
                    String cc = c.passed ? "#16a34a" : "#dc2626";
                    checksHtml.append("<span style=\"color:").append(cc)
                            .append(";font-size:11px;\">").append(c.passed ? "✓" : "✗")
                            .append(" [").append(c.source).append("] ")
                            .append(escHtml(c.detail)).append("</span><br>");
                }
                if (s.errorMessage != null) {
                    checksHtml.append("<span style=\"color:#dc2626;font-size:11px;\">✗ Error: ")
                            .append(escHtml(s.errorMessage)).append("</span>");
                }

                StringBuilder extractedHtml = new StringBuilder();
                for (java.util.Map.Entry<String, String> e : s.extractedVariables.entrySet()) {
                    extractedHtml.append("<code style=\"font-size:11px;color:#7c3aed;\">")
                            .append(escHtml(e.getKey())).append("</code>")
                            .append(" = <code style=\"font-size:11px;\">")
                            .append(escHtml(truncate(e.getValue(), 80))).append("</code><br>");
                }

                stepsHtml.append("<details style=\"border:1px solid #e2e8f0;border-radius:10px;"
                                + "margin-bottom:10px;overflow:hidden;\">\n")
                        .append("<summary style=\"padding:12px 16px;cursor:pointer;background:#f8fafc;"
                                + "display:flex;align-items:center;gap:10px;list-style:none;"
                                + "user-select:none;\">\n")
                        .append("<span style=\"color:#64748b;font-size:12px;min-width:24px;\">").append(idx).append("</span>")
                        .append(pill)
                        .append("<span style=\"font-weight:600;flex:1;font-size:13px;\">").append(escHtml(s.stepIdentifier)).append("</span>")
                        .append("<span style=\"font-size:12px;color:#64748b;font-family:monospace;\">")
                        .append(s.method).append(" ").append(escHtml(truncate(s.resolvedUrl, 60))).append("</span>")
                        .append("<span style=\"font-size:11px;color:#94a3b8;margin-left:12px;\">").append(s.durationMs).append(" ms</span>")
                        .append("</summary>\n")
                        .append("<div style=\"padding:16px;background:#fff;display:grid;gap:12px;\">\n");

                stepsHtml.append(section("URL",
                        "<code style=\"word-break:break-all;font-size:12px;\">" + escHtml(s.resolvedUrl) + "</code>"));
                stepsHtml.append(section("Request Headers", formatMapHtml(s.requestHeaders)));
                if (s.requestBody != null && !s.requestBody.isBlank())
                    stepsHtml.append(section("Request Body",
                            "<pre style=\"margin:0;font-size:11px;overflow-x:auto;white-space:pre-wrap;word-break:break-all;\">"
                                    + escHtml(s.requestBody) + "</pre>"));
                stepsHtml.append(section("Status",
                        "<code style=\"font-size:13px;font-weight:700;color:" + sc + ";\">"
                                + s.statusCode + "</code>"));
                if (!s.responseHeaders.isBlank())
                    stepsHtml.append(section("Response Headers",
                            "<pre style=\"margin:0;font-size:11px;overflow-x:auto;\">"
                                    + escHtml(s.responseHeaders) + "</pre>"));
                if (s.responseBody != null && !s.responseBody.isBlank())
                    stepsHtml.append(section("Response Body",
                            "<pre style=\"margin:0;font-size:11px;overflow-x:auto;white-space:pre-wrap;word-break:break-all;\">"
                                    + escHtml(truncate(s.responseBody, 4000)) + "</pre>"));
                if (checksHtml.length() > 0) stepsHtml.append(section("Checks", checksHtml.toString()));
                if (extractedHtml.length() > 0) stepsHtml.append(section("Extracted Variables", extractedHtml.toString()));

                stepsHtml.append("</div></details>\n");
                idx++;
            }

            return "<!DOCTYPE html><html lang=\"en\"><head>"
                    + "<meta charset=\"UTF-8\">"
                    + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                    + "<title>Run Report — " + escHtml(report.flowName) + "</title>"
                    + "<style>*{box-sizing:border-box}"
                    + "body{margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;"
                    + "background:#f1f5f9;color:#0f172a}"
                    + "details>summary::-webkit-details-marker{display:none}"
                    + "details[open]>summary{background:#f0f9ff;border-bottom:1px solid #e2e8f0}"
                    + "</style></head><body>\n"
                    + "<div style=\"background:#0f172a;color:#f8fafc;padding:18px 32px;"
                    + "position:sticky;top:0;z-index:10;display:flex;align-items:center;gap:14px;\">\n"
                    + "<span style=\"font-size:17px;font-weight:700;\">▣ API Flow Run Report</span>\n"
                    + "<span style=\"background:" + statusBg + ";color:" + statusColor
                    + ";padding:3px 14px;border-radius:99px;font-weight:700;font-size:12px;\">"
                    + overallStatus + "</span>\n"
                    + "<span style=\"color:#94a3b8;font-size:12px;margin-left:auto;\">"
                    + escHtml(report.flowName) + " &nbsp;·&nbsp; " + report.executedAt + "</span>\n"
                    + "</div>\n"
                    + "<div style=\"max-width:960px;margin:28px auto;padding:0 16px;\">\n"
                    + "<div style=\"display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-bottom:20px;\">\n"
                    + statCard("Total Steps", String.valueOf(report.totalSteps), "#2563eb", "#eff6ff")
                    + statCard("Passed", String.valueOf(report.passedSteps), "#16a34a", "#dcfce7")
                    + statCard("Failed", String.valueOf(report.failedSteps),
                    report.failedSteps > 0 ? "#dc2626" : "#16a34a",
                    report.failedSteps > 0 ? "#fee2e2" : "#dcfce7")
                    + statCard("Duration", report.totalDurationMs + " ms", "#7c3aed", "#f5f3ff")
                    + "</div>\n"
                    + stepsHtml
                    + "</div>\n</body></html>";
        }

        private static String section(String title, String content) {
            return "<div>\n<div style=\"font-size:10px;font-weight:700;color:#64748b;"
                    + "text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px;\">"
                    + title + "</div>\n"
                    + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;"
                    + "border-radius:6px;padding:8px 10px;\">"
                    + content + "</div>\n</div>\n";
        }

        private static String statCard(String label, String value, String color, String bg) {
            return "<div style=\"background:" + bg + ";border:1px solid " + color + "33;"
                    + "border-radius:10px;padding:14px;text-align:center;\">\n"
                    + "<div style=\"font-size:22px;font-weight:700;color:" + color + ";\">"
                    + value + "</div>\n"
                    + "<div style=\"font-size:11px;color:" + color + ";opacity:.75;margin-top:2px;\">"
                    + label + "</div>\n</div>\n";
        }

        private static String formatMapHtml(java.util.Map<String, String> map) {
            if (map == null || map.isEmpty()) {
                return "<span style=\"color:#94a3b8;font-size:12px;\">—</span>";
            }
            StringBuilder sb = new StringBuilder();
            for (java.util.Map.Entry<String, String> e : map.entrySet()) {
                sb.append("<code style=\"font-size:11px;color:#0369a1;\">")
                        .append(escHtml(e.getKey())).append("</code>: ")
                        .append("<code style=\"font-size:11px;\">").append(escHtml(e.getValue()))
                        .append("</code><br>");
            }
            return sb.toString();
        }

        private static String escHtml(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
        }

        private static String truncate(String s, int max) {
            if (s == null) return "";
            return s.length() <= max ? s : s.substring(0, max) + "…";
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String name = "default.flow.meta";
            if (query != null && query.startsWith("name=")) {
                name = query.substring("name=".length());
            }

            Path flowDir = Paths.get("").toAbsolutePath().resolve(FLOW_DIR);
            String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path flowFile = flowDir.resolve(safeName + ".json");
            if (!Files.exists(flowFile)) {
                JsonObject err = new JsonObject();
                err.addProperty("error", "Flow not found: " + safeName);
                sendJson(exchange, 404, err.toString());
                return;
            }

            JsonObject flow;
            try {
                flow = JsonParser.parseString(
                        Files.readString(flowFile, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception ex) {
                JsonObject err = new JsonObject();
                err.addProperty("error", "Invalid flow JSON: " + ex.getMessage());
                sendJson(exchange, 400, err.toString());
                return;
            }

            FlowRunner.RunReport report = FlowRunner.runFlow(safeName, flow);

            // Write HTML report to site/
            String reportFileName = "report-run-" + safeName + ".html";
            Path siteDir = resolveStaticDir();
            try {
                Files.createDirectories(siteDir);
                Files.writeString(siteDir.resolve(reportFileName),
                        buildHtmlReport(report), StandardCharsets.UTF_8);
            } catch (Exception ex) {
                System.err.println("Warning: could not write HTML report: " + ex.getMessage());
            }

            // Return JSON summary
            JsonObject out = new JsonObject();
            out.addProperty("flowName", report.flowName);
            out.addProperty("executedAt", report.executedAt);
            out.addProperty("totalDurationMs", report.totalDurationMs);
            out.addProperty("totalSteps", report.totalSteps);
            out.addProperty("passedSteps", report.passedSteps);
            out.addProperty("failedSteps", report.failedSteps);
            out.addProperty("reportUrl", "/" + reportFileName);
            out.add("steps", buildStepsSummaryJson(report.steps));
            sendJson(exchange, 200, PRETTY.toJson(out));
        }
    }
}
