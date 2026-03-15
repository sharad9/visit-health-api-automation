# Collection Runner Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a server-side "Run Collection" feature that executes all steps in a flow sequentially (like Postman collection runner), extracts variables between steps, evaluates checks, and generates an HTML execution report viewable in the browser.

**Architecture:** A new `FlowRunner.java` in the server package owns all execution logic (token substitution, HTTP dispatch, JSONPath extraction, check evaluation, report building). `FlowServer.java` adds a `FlowRunHandler` that calls `FlowRunner`, writes an HTML report to `site/`, and returns a JSON summary. The GWT UI adds a "▶ Run Collection" button in `NavbarView` wired to POST `/api/flows/run` and shows a result overlay with a link to the report.

**Tech Stack:** Java 11, Gson 2.11, `com.sun.net.httpserver`, GWT 2.10 (client), existing `HttpURLConnection` proxy pattern

---

## Chunk 1: Server-side FlowRunner

### Task 1: FlowRunner.java — token substitution + JSONPath

**Files:**
- Create: `src/main/java/com/visit/gwtapiflowbuilder/server/FlowRunner.java`

This class holds all pure logic with no I/O (easy to reason about):
- `substitute(String template, Map<String,String> vars)` — replaces `{{KEY}}`
- `resolveJsonPath(String json, String path)` — handles `$.a`, `$.a.b`, `$.a[0].b`
- `extractHeaderValue(String rawHeaders, String headerName)` — parses `key: value\n` text

- [ ] **Step 1: Create FlowRunner.java with substitution + JSONPath**

```java
package com.visit.gwtapiflowbuilder.server;

import com.google.gson.*;

import java.util.*;

public final class FlowRunner {

    // --- Token substitution ---

    static String substitute(String template, Map<String, String> vars) {
        if (template == null) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int start = template.indexOf("{{", i);
            if (start < 0) { sb.append(template.substring(i)); break; }
            sb.append(template.substring(i, start));
            int end = template.indexOf("}}", start + 2);
            if (end < 0) { sb.append(template.substring(start)); break; }
            String key = template.substring(start + 2, end);
            sb.append(vars.getOrDefault(key, template.substring(start, end + 2)));
            i = end + 2;
        }
        return sb.toString();
    }

    static Map<String, String> mergedVars(Map<String, String> globalInputs,
                                          Map<String, String> envVars,
                                          Map<String, String> runtimeVars) {
        Map<String, String> merged = new LinkedHashMap<>();
        merged.putAll(globalInputs);
        merged.putAll(envVars);
        merged.putAll(runtimeVars); // highest priority
        return merged;
    }

    // --- JSONPath (subset: $.a, $.a.b, $.a[0].b) ---

    static String resolveJsonPath(String json, String path) {
        if (json == null || json.isBlank() || path == null) return null;
        try {
            JsonElement root = JsonParser.parseString(json);
            if (!path.startsWith("$.")) return null;
            String[] parts = path.substring(2).split("\\.");
            JsonElement current = root;
            for (String part : parts) {
                if (current == null || current.isJsonNull()) return null;
                if (part.contains("[")) {
                    String field = part.substring(0, part.indexOf('['));
                    int idx = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));
                    if (!field.isEmpty()) {
                        if (!current.isJsonObject()) return null;
                        current = current.getAsJsonObject().get(field);
                    }
                    if (current == null || !current.isJsonArray()) return null;
                    current = current.getAsJsonArray().get(idx);
                } else {
                    if (!current.isJsonObject()) return null;
                    current = current.getAsJsonObject().get(part);
                }
            }
            if (current == null || current.isJsonNull()) return null;
            return current.isJsonPrimitive() ? current.getAsString() : current.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // --- Header extraction (parses "key: value\n" raw header text) ---

    static String extractHeaderValue(String rawHeaders, String headerName) {
        if (rawHeaders == null || headerName == null) return null;
        String lower = headerName.toLowerCase();
        for (String line : rawHeaders.split("\n")) {
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            if (line.substring(0, colon).trim().toLowerCase().equals(lower)) {
                return line.substring(colon + 1).trim();
            }
        }
        return null;
    }
}
```

- [ ] **Step 2: Build to verify it compiles**

```bash
cd /Users/sharadrakeshmaddheshiya/Desktop/vanilla/gwt-api-flow-builder
mvn -q -DskipTests clean package
```
Expected: no output (clean build)

---

### Task 2: FlowRunner.java — step execution + extraction + checks

**Files:**
- Modify: `src/main/java/com/visit/gwtapiflowbuilder/server/FlowRunner.java`

Add:
- `StepResult` inner record/class — holds all data for one step execution
- `RunReport` inner class — holds all step results + summary
- `runFlow(JsonObject flowJson)` — main entry: parses flow, executes all steps, returns `RunReport`
- `executeStep(...)` — builds and fires one HTTP request, returns raw response
- `extractVariables(...)` — populates runtimeVars from extraction config
- `evaluateChecks(...)` — evaluates check list, returns list of check results

- [ ] **Step 3: Add StepResult, RunReport, and runFlow to FlowRunner.java**

```java
// --- Data classes ---

public static final class CheckResult {
    public final String source;
    public final String detail;
    public final boolean passed;
    CheckResult(String source, boolean passed, String detail) {
        this.source = source; this.passed = passed; this.detail = detail;
    }
}

public static final class StepResult {
    public final String stepIdentifier;
    public final String method;
    public final String resolvedUrl;
    public final Map<String, String> requestHeaders;
    public final String requestBody;
    public final int statusCode;
    public final String responseHeaders;
    public final String responseBody;
    public final long durationMs;
    public final Map<String, String> extractedVariables;
    public final List<CheckResult> checks;
    public final boolean passed;
    public final String errorMessage;

    StepResult(String stepIdentifier, String method, String resolvedUrl,
               Map<String, String> requestHeaders, String requestBody,
               int statusCode, String responseHeaders, String responseBody,
               long durationMs, Map<String, String> extractedVariables,
               List<CheckResult> checks, boolean passed, String errorMessage) {
        this.stepIdentifier = stepIdentifier;
        this.method = method;
        this.resolvedUrl = resolvedUrl;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.durationMs = durationMs;
        this.extractedVariables = extractedVariables;
        this.checks = checks;
        this.passed = passed;
        this.errorMessage = errorMessage;
    }
}

public static final class RunReport {
    public final String flowName;
    public final String executedAt;
    public final long totalDurationMs;
    public final int totalSteps;
    public final int passedSteps;
    public final int failedSteps;
    public final List<StepResult> steps;

    RunReport(String flowName, String executedAt, long totalDurationMs,
              List<StepResult> steps) {
        this.flowName = flowName;
        this.executedAt = executedAt;
        this.totalDurationMs = totalDurationMs;
        this.steps = steps;
        this.totalSteps = steps.size();
        this.passedSteps = (int) steps.stream().filter(s -> s.passed).count();
        this.failedSteps = totalSteps - passedSteps;
    }
}
```

- [ ] **Step 4: Add runFlow() to FlowRunner.java**

```java
public static RunReport runFlow(String flowName, JsonObject flow) {
    // 1. Extract globalInputs
    Map<String, String> globalInputs = new LinkedHashMap<>();
    JsonObject gi = flow.has("globalInputs") ? flow.getAsJsonObject("globalInputs") : null;
    if (gi != null) {
        for (String k : gi.keySet()) globalInputs.put(k, gi.get(k).getAsString());
    }

    // 2. Extract active environment variables
    Map<String, String> envVars = new LinkedHashMap<>();
    JsonObject environments = flow.has("environments") ? flow.getAsJsonObject("environments") : null;
    if (environments != null) {
        String activeId = environments.has("activeEnvironmentId")
            ? environments.get("activeEnvironmentId").getAsString() : null;
        JsonArray items = environments.has("environmentItems")
            ? environments.getAsJsonArray("environmentItems") : new JsonArray();
        for (JsonElement el : items) {
            JsonObject env = el.getAsJsonObject();
            String id = env.has("environmentId") ? env.get("environmentId").getAsString() : null;
            if (Objects.equals(id, activeId) && env.has("environmentVariables")) {
                JsonObject evs = env.getAsJsonObject("environmentVariables");
                for (String k : evs.keySet()) envVars.put(k, evs.get(k).getAsString());
                break;
            }
        }
    }

    // 3. Execute steps
    Map<String, String> runtimeVars = new LinkedHashMap<>();
    List<StepResult> results = new ArrayList<>();
    long flowStart = System.currentTimeMillis();

    JsonArray steps = flow.has("steps") ? flow.getAsJsonArray("steps") : new JsonArray();
    for (JsonElement stepEl : steps) {
        StepResult result = executeStep(stepEl.getAsJsonObject(), globalInputs, envVars, runtimeVars);
        results.add(result);
        runtimeVars.putAll(result.extractedVariables);
    }

    long totalMs = System.currentTimeMillis() - flowStart;
    return new RunReport(flowName, nowFormatted(), totalMs, results);
}
```

- [ ] **Step 5: Add executeStep() to FlowRunner.java**

```java
private static StepResult executeStep(JsonObject step, Map<String, String> globalInputs,
                                       Map<String, String> envVars, Map<String, String> runtimeVars) {
    String stepId = step.has("stepIdentifier") ? step.get("stepIdentifier").getAsString() : "UNKNOWN";
    JsonObject request = step.has("request") ? step.getAsJsonObject("request") : new JsonObject();

    Map<String, String> vars = mergedVars(globalInputs, envVars, runtimeVars);

    String method = request.has("httpMethod") ? request.get("httpMethod").getAsString() : "GET";
    String url = substitute(request.has("requestUrl") ? request.get("requestUrl").getAsString() : "", vars);
    int timeoutMs = request.has("timeoutMilliseconds") ? request.get("timeoutMilliseconds").getAsInt() : 10000;

    // Resolve headers
    Map<String, String> headers = new LinkedHashMap<>();
    if (request.has("headers") && request.get("headers").isJsonObject()) {
        JsonObject h = request.getAsJsonObject("headers");
        for (String k : h.keySet()) headers.put(k, substitute(h.get(k).getAsString(), vars));
    }

    // Resolve body
    String bodyStr = "";
    if (request.has("body") && request.get("body").isJsonObject()) {
        JsonObject body = request.getAsJsonObject("body");
        String type = body.has("type") ? body.get("type").getAsString() : "application/json";
        if ("multipart/form-data".equals(type)) {
            bodyStr = "[multipart — skipped]";
        } else {
            bodyStr = substituteJsonObject(body, vars).toString();
        }
    }

    long start = System.currentTimeMillis();
    int statusCode = -1;
    String responseHeaders = "";
    String responseBody = "";
    String errorMessage = null;

    try {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs * 2);
        conn.setRequestMethod(method.toUpperCase());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String k = e.getKey().toLowerCase();
            if ("host".equals(k) || "content-length".equals(k)) continue;
            conn.setRequestProperty(e.getKey(), e.getValue());
        }
        if (!bodyStr.isEmpty() && !bodyStr.equals("[multipart — skipped]")
                && !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            conn.setDoOutput(true);
            conn.getOutputStream().write(bodyStr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        statusCode = conn.getResponseCode();
        java.io.InputStream stream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        responseBody = stream == null ? "" : readStream(stream);
        responseHeaders = formatHeaders(conn.getHeaderFields());
        conn.disconnect();
    } catch (Exception ex) {
        errorMessage = ex.getMessage();
    }

    long durationMs = System.currentTimeMillis() - start;

    // Extract variables
    Map<String, String> extracted = extractVariables(stepId, step, responseBody, responseHeaders);

    // Evaluate checks
    List<CheckResult> checkResults = evaluateChecks(step, statusCode, responseBody, responseHeaders);
    boolean passed = errorMessage == null && checkResults.stream().allMatch(c -> c.passed);

    return new StepResult(stepId, method, url, headers, bodyStr,
                          statusCode, responseHeaders, responseBody,
                          durationMs, extracted, checkResults, passed, errorMessage);
}
```

- [ ] **Step 6: Add helper methods to FlowRunner.java**

```java
private static Map<String, String> extractVariables(String stepId, JsonObject step,
                                                      String responseBody, String responseHeaders) {
    Map<String, String> vars = new LinkedHashMap<>();
    if (!step.has("extraction")) return vars;
    JsonObject extraction = step.getAsJsonObject("extraction");

    if (extraction.has("bodyJsonPaths") && extraction.get("bodyJsonPaths").isJsonObject()) {
        JsonObject paths = extraction.getAsJsonObject("bodyJsonPaths");
        for (String varName : paths.keySet()) {
            String path = paths.get(varName).getAsString();
            String value = resolveJsonPath(responseBody, path);
            if (value != null) vars.put(stepId + "_REQUEST_BODY_" + varName, value);
        }
    }

    if (extraction.has("headerValues") && extraction.get("headerValues").isJsonObject()) {
        JsonObject hvs = extraction.getAsJsonObject("headerValues");
        for (String varName : hvs.keySet()) {
            String headerName = hvs.get(varName).getAsString();
            String value = extractHeaderValue(responseHeaders, headerName);
            if (value != null) vars.put(stepId + "_REQUEST_HEADER_" + varName, value);
        }
    }
    return vars;
}

private static List<CheckResult> evaluateChecks(JsonObject step, int statusCode,
                                                  String responseBody, String responseHeaders) {
    List<CheckResult> results = new ArrayList<>();
    if (!step.has("checks")) return results;
    for (JsonElement el : step.getAsJsonArray("checks")) {
        JsonObject check = el.getAsJsonObject();
        String source = check.has("source") ? check.get("source").getAsString() : "";
        switch (source) {
            case "status":
                boolean statusOk = statusCode >= 200 && statusCode < 300;
                results.add(new CheckResult("status", statusOk,
                    statusOk ? "Status " + statusCode + " OK" : "Status " + statusCode + " FAILED"));
                break;
            case "body":
                if (check.has("jsonPath")) {
                    String path = check.get("jsonPath").getAsString();
                    String val = resolveJsonPath(responseBody, path);
                    if (check.has("equals")) {
                        String expected = check.get("equals").getAsString();
                        boolean eq = expected.equals(val);
                        results.add(new CheckResult("body", eq,
                            path + (eq ? " == " : " != ") + expected));
                    } else if (check.has("exists") && check.get("exists").getAsBoolean()) {
                        boolean ex = val != null;
                        results.add(new CheckResult("body", ex,
                            path + (ex ? " exists" : " not found")));
                    }
                } else {
                    boolean bodyOk = responseBody != null && !responseBody.isBlank();
                    results.add(new CheckResult("body", bodyOk,
                        bodyOk ? "Body non-empty" : "Body empty"));
                }
                break;
            default:
                results.add(new CheckResult(source, true, "skipped"));
        }
    }
    return results;
}

// Recursively substitute tokens in all string values of a JsonObject
private static JsonObject substituteJsonObject(JsonObject obj, Map<String, String> vars) {
    JsonObject result = new JsonObject();
    for (String k : obj.keySet()) {
        JsonElement v = obj.get(k);
        if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
            result.addProperty(k, substitute(v.getAsString(), vars));
        } else if (v.isJsonObject()) {
            result.add(k, substituteJsonObject(v.getAsJsonObject(), vars));
        } else if (v.isJsonArray()) {
            result.add(k, substituteJsonArray(v.getAsJsonArray(), vars));
        } else {
            result.add(k, v);
        }
    }
    return result;
}

private static JsonArray substituteJsonArray(JsonArray arr, Map<String, String> vars) {
    JsonArray result = new JsonArray();
    for (JsonElement el : arr) {
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            result.add(substitute(el.getAsString(), vars));
        } else if (el.isJsonObject()) {
            result.add(substituteJsonObject(el.getAsJsonObject(), vars));
        } else if (el.isJsonArray()) {
            result.add(substituteJsonArray(el.getAsJsonArray(), vars));
        } else {
            result.add(el);
        }
    }
    return result;
}

private static String readStream(java.io.InputStream stream) throws java.io.IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[4096]; int n;
    while ((n = stream.read(buf)) != -1) out.write(buf, 0, n);
    return out.toString(java.nio.charset.StandardCharsets.UTF_8);
}

private static String formatHeaders(java.util.Map<String, java.util.List<String>> headers) {
    if (headers == null) return "";
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, java.util.List<String>> e : headers.entrySet()) {
        if (e.getKey() == null) continue;
        for (String v : e.getValue()) sb.append(e.getKey()).append(": ").append(v).append("\n");
    }
    return sb.toString().trim();
}

private static String nowFormatted() {
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    return String.format("%04d-%02d-%02d %02d:%02d:%02d",
        now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
        now.getHour(), now.getMinute(), now.getSecond());
}
```

- [ ] **Step 7: Build to verify FlowRunner compiles**

```bash
mvn -q -DskipTests clean package
```
Expected: no output

---

### Task 3: FlowServer.java — /api/flows/run handler + HTML report generation

**Files:**
- Modify: `src/main/java/com/visit/gwtapiflowbuilder/server/FlowServer.java`

Add `FlowRunHandler` inner class. Register it at `/api/flows/run`.

- [ ] **Step 8: Register /api/flows/run in FlowServer.main()**

In the `main` method, add before `server.setExecutor(null)`:
```java
server.createContext("/api/flows/run", new FlowRunHandler());
```

- [ ] **Step 9: Add FlowRunHandler inner class to FlowServer.java**

```java
static class FlowRunHandler implements HttpHandler {
    private static final String FLOW_DIR_NAME = "flows";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

        Path flowDir = Paths.get("").toAbsolutePath().resolve(FLOW_DIR_NAME);
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
            flow = JsonParser.parseString(Files.readString(flowFile, StandardCharsets.UTF_8))
                             .getAsJsonObject();
        } catch (Exception ex) {
            JsonObject err = new JsonObject();
            err.addProperty("error", "Invalid flow JSON: " + ex.getMessage());
            sendJson(exchange, 400, err.toString());
            return;
        }

        FlowRunner.RunReport report = FlowRunner.runFlow(safeName, flow);

        // Write HTML report
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
        sendJson(exchange, 200, GSON.toJson(out));
    }

    private JsonArray buildStepsSummaryJson(List<FlowRunner.StepResult> steps) {
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
}
```

- [ ] **Step 10: Add buildHtmlReport() to FlowRunHandler**

```java
private String buildHtmlReport(FlowRunner.RunReport report) {
    String overallStatus = report.failedSteps == 0 ? "PASS" : "FAIL";
    String statusColor = report.failedSteps == 0 ? "#16a34a" : "#dc2626";
    String statusBg   = report.failedSteps == 0 ? "#dcfce7" : "#fee2e2";

    StringBuilder steps = new StringBuilder();
    int idx = 1;
    for (FlowRunner.StepResult s : report.steps) {
        String sc = s.passed ? "#16a34a" : "#dc2626";
        String sb2 = s.passed ? "#dcfce7" : "#fee2e2";
        String pill = "<span style=\"background:" + sb2 + ";color:" + sc
            + ";padding:2px 10px;border-radius:99px;font-size:11px;font-weight:700;\">"
            + (s.passed ? "PASS" : "FAIL") + "</span>";

        StringBuilder checksHtml = new StringBuilder();
        for (FlowRunner.CheckResult c : s.checks) {
            String cc = c.passed ? "#16a34a" : "#dc2626";
            checksHtml.append("<span style=\"color:").append(cc)
                .append(";font-size:11px;\">").append(c.passed ? "✓" : "✗")
                .append(" [").append(c.source).append("] ").append(c.detail)
                .append("</span><br>");
        }
        if (s.errorMessage != null) {
            checksHtml.append("<span style=\"color:#dc2626;font-size:11px;\">✗ Error: ")
                .append(escapeHtml(s.errorMessage)).append("</span>");
        }

        StringBuilder extractedHtml = new StringBuilder();
        for (Map.Entry<String, String> e : s.extractedVariables.entrySet()) {
            extractedHtml.append("<code style=\"font-size:11px;color:#7c3aed;\">")
                .append(escapeHtml(e.getKey())).append("</code>")
                .append(" = <code style=\"font-size:11px;\">")
                .append(escapeHtml(truncate(e.getValue(), 80))).append("</code><br>");
        }

        steps.append("<details style=\"border:1px solid #e2e8f0;border-radius:10px;"
            + "margin-bottom:10px;overflow:hidden;\">\n")
            .append("<summary style=\"padding:12px 16px;cursor:pointer;background:#f8fafc;"
                + "display:flex;align-items:center;gap:10px;list-style:none;\">\n")
            .append("<span style=\"color:#64748b;font-size:12px;min-width:24px;\">").append(idx).append("</span>")
            .append(pill)
            .append("<span style=\"font-weight:600;flex:1;font-size:13px;\">").append(escapeHtml(s.stepIdentifier)).append("</span>")
            .append("<span style=\"font-size:12px;color:#64748b;font-family:monospace;\">")
                .append(s.method).append(" ").append(escapeHtml(truncate(s.resolvedUrl, 60))).append("</span>")
            .append("<span style=\"font-size:11px;color:#94a3b8;margin-left:12px;\">").append(s.durationMs).append(" ms</span>")
            .append("</summary>\n")
            .append("<div style=\"padding:16px;background:#fff;display:grid;gap:12px;\">\n");

        steps.append(section("URL", "<code style=\"word-break:break-all;font-size:12px;\">"
            + escapeHtml(s.resolvedUrl) + "</code>"));
        steps.append(section("Request Headers", formatMapHtml(s.requestHeaders)));
        if (s.requestBody != null && !s.requestBody.isBlank()) {
            steps.append(section("Request Body", "<pre style=\"margin:0;font-size:11px;overflow-x:auto;\">"
                + escapeHtml(s.requestBody) + "</pre>"));
        }
        steps.append(section("Status", "<code style=\"font-size:13px;font-weight:700;color:" + sc + ";\">"
            + s.statusCode + "</code>"));
        if (!s.responseHeaders.isBlank()) {
            steps.append(section("Response Headers",
                "<pre style=\"margin:0;font-size:11px;overflow-x:auto;\">"
                + escapeHtml(s.responseHeaders) + "</pre>"));
        }
        if (s.responseBody != null && !s.responseBody.isBlank()) {
            steps.append(section("Response Body",
                "<pre style=\"margin:0;font-size:11px;overflow-x:auto;white-space:pre-wrap;word-break:break-all;\">"
                + escapeHtml(truncate(s.responseBody, 4000)) + "</pre>"));
        }
        if (checksHtml.length() > 0) {
            steps.append(section("Checks", checksHtml.toString()));
        }
        if (extractedHtml.length() > 0) {
            steps.append(section("Extracted Variables", extractedHtml.toString()));
        }

        steps.append("</div></details>\n");
        idx++;
    }

    return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
        + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
        + "<title>Run Report — " + escapeHtml(report.flowName) + "</title>"
        + "<style>*{box-sizing:border-box}body{margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;"
        + "background:#f1f5f9;color:#0f172a}details>summary::-webkit-details-marker{display:none}</style>"
        + "</head><body>\n"
        + "<div style=\"background:#0f172a;color:#f8fafc;padding:20px 32px;position:sticky;top:0;z-index:10;"
        + "display:flex;align-items:center;gap:16px;\">\n"
        + "<span style=\"font-size:18px;font-weight:700;\">▣ API Flow Run Report</span>\n"
        + "<span style=\"background:" + statusBg + ";color:" + statusColor
        + ";padding:3px 14px;border-radius:99px;font-weight:700;font-size:13px;\">" + overallStatus + "</span>\n"
        + "<span style=\"color:#94a3b8;font-size:12px;margin-left:auto;\">"
        + escapeHtml(report.flowName) + " &nbsp;·&nbsp; " + report.executedAt + "</span>\n"
        + "</div>\n"
        + "<div style=\"max-width:960px;margin:32px auto;padding:0 16px;\">\n"
        + "<div style=\"display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:24px;\">\n"
        + statCard("Total Steps", String.valueOf(report.totalSteps), "#2563eb", "#eff6ff")
        + statCard("Passed", String.valueOf(report.passedSteps), "#16a34a", "#dcfce7")
        + statCard("Failed", String.valueOf(report.failedSteps), "#dc2626", "#fee2e2")
        + statCard("Duration", report.totalDurationMs + " ms", "#7c3aed", "#f5f3ff")
        + "</div>\n"
        + steps
        + "</div>\n</body></html>";
}

private static String section(String title, String content) {
    return "<div>\n<div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;"
        + "letter-spacing:.05em;margin-bottom:4px;\">" + title + "</div>\n"
        + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;padding:8px 10px;\">"
        + content + "</div>\n</div>\n";
}

private static String statCard(String label, String value, String color, String bg) {
    return "<div style=\"background:" + bg + ";border:1px solid " + color + "33;"
        + "border-radius:10px;padding:16px;text-align:center;\">\n"
        + "<div style=\"font-size:24px;font-weight:700;color:" + color + ";\">" + value + "</div>\n"
        + "<div style=\"font-size:12px;color:" + color + ";opacity:.7;margin-top:2px;\">" + label + "</div>\n"
        + "</div>\n";
}

private static String formatMapHtml(Map<String, String> map) {
    if (map == null || map.isEmpty()) return "<span style=\"color:#94a3b8;font-size:12px;\">—</span>";
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : map.entrySet()) {
        sb.append("<code style=\"font-size:11px;color:#0369a1;\">").append(escapeHtml(e.getKey()))
          .append("</code>: <code style=\"font-size:11px;\">").append(escapeHtml(e.getValue()))
          .append("</code><br>");
    }
    return sb.toString();
}

private static String escapeHtml(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
}

private static String truncate(String s, int max) {
    if (s == null) return "";
    return s.length() <= max ? s : s.substring(0, max) + "…";
}
```

- [ ] **Step 11: Add missing import for Map in FlowRunHandler**

Verify `java.util.Map` and `java.util.List` are already imported in `FlowServer.java` (they are used by other handlers). If not, add them.

- [ ] **Step 12: Build**

```bash
mvn -q -DskipTests clean package
```
Expected: no output (clean build)

- [ ] **Step 13: Smoke test the run endpoint**

```bash
curl -s "http://localhost:8000/api/flows/run?name=auth-pharmacy-upload-prescription.flow.meta" | python3 -m json.tool | head -20
```
Expected: JSON with `flowName`, `totalSteps`, `passedSteps`, `failedSteps`, `reportUrl`, `steps[]`

---

## Chunk 2: UI — Run Collection button + Result overlay

### Task 4: NavbarView.java — add runButton field + button in navbar

**Files:**
- Modify: `src/main/java/com/visit/gwtapiflowbuilder/client/ui/NavbarView.java`

- [ ] **Step 14: Add runButton field and wire it into the navbar right section**

Add before `saveButton` in the right section:
```java
public Button runButton;
```

In `build()`, before adding `saveButton` to `right`:
```java
runButton = UiFactory.outlineButton("▶ Run Collection");
runButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
runButton.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
runButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEMPLATE);
runButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_COLOR, Theme.COLOR_TEMPLATE);
if (onRun != null) {
    runButton.addClickHandler(onRun);
}
right.add(runButton);
```

Update `build()` signature:
```java
public FlowPanel build(ClickHandler onToggle, String toggleLabel,
                       ClickHandler onSave, ClickHandler onRun, ListBox flowSelect)
```

- [ ] **Step 15: Build after NavbarView change**

```bash
mvn -q -DskipTests clean package
```
Expected: compile error in GwtApiFlowBuilder where `navbarView.build(...)` is called with 4 args — fix in next step.

---

### Task 5: GwtApiFlowBuilder.java — wire Run Collection button + show result overlay

**Files:**
- Modify: `src/main/java/com/visit/gwtapiflowbuilder/client/GwtApiFlowBuilder.java`

- [ ] **Step 16: Update navbarView.build() call to pass onRun handler**

Find the `navbarView.build(...)` call and add a 4th argument (the run handler) before `flowSelect`:
```java
navbarView.build(onToggle, toggleLabel, saveHandler(), this::runCollection, flowSelect)
```

- [ ] **Step 17: Add runCollection() method**

```java
private void runCollection() {
    String flowName = metaIdBox.getValue();
    if (flowName == null || flowName.isBlank()) {
        Window.alert("Save the flow first before running.");
        return;
    }
    if (navbarView.runButton != null) {
        navbarView.runButton.setText("⏳ Running…");
        navbarView.runButton.setEnabled(false);
    }

    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
        "/api/flows/run?name=" + flowName);
    rb.setHeader("Content-Type", "application/json");
    try {
        rb.sendRequest(null, new RequestCallback() {
            @Override
            public void onResponseReceived(Request req, Response resp) {
                if (navbarView.runButton != null) {
                    navbarView.runButton.setText("▶ Run Collection");
                    navbarView.runButton.setEnabled(true);
                }
                if (resp.getStatusCode() == 200) {
                    showRunReport(resp.getText());
                } else {
                    Window.alert("Run failed: " + resp.getStatusCode() + "\n" + resp.getText());
                }
            }
            @Override
            public void onError(Request req, Throwable ex) {
                if (navbarView.runButton != null) {
                    navbarView.runButton.setText("▶ Run Collection");
                    navbarView.runButton.setEnabled(true);
                }
                Window.alert("Run error: " + ex.getMessage());
            }
        });
    } catch (RequestException ex) {
        if (navbarView.runButton != null) {
            navbarView.runButton.setText("▶ Run Collection");
            navbarView.runButton.setEnabled(true);
        }
        Window.alert("Run error: " + ex.getMessage());
    }
}
```

- [ ] **Step 18: Add showRunReport() method — result overlay**

```java
private void showRunReport(String json) {
    JSONObject obj;
    try {
        obj = JSONParser.parseStrict(json).isObject();
    } catch (Exception e) {
        Window.alert("Could not parse report: " + e.getMessage());
        return;
    }
    if (obj == null) {
        Window.alert("Unexpected report response.");
        return;
    }

    int total   = (int) obj.get("totalSteps").isNumber().doubleValue();
    int passed  = (int) obj.get("passedSteps").isNumber().doubleValue();
    int failed  = (int) obj.get("failedSteps").isNumber().doubleValue();
    long ms     = (long) obj.get("totalDurationMs").isNumber().doubleValue();
    String reportUrl = obj.get("reportUrl").isString().stringValue();
    String execAt = obj.get("executedAt").isString().stringValue();

    // Overlay backdrop
    FlowPanel overlay = new FlowPanel();
    overlay.getElement().getStyle().setProperty("position", "fixed");
    overlay.getElement().getStyle().setProperty("inset", "0");
    overlay.getElement().getStyle().setProperty("background", "rgba(15,23,42,0.55)");
    overlay.getElement().getStyle().setProperty("zIndex", "100");
    overlay.getElement().getStyle().setProperty("display", "flex");
    overlay.getElement().getStyle().setProperty("alignItems", "center");
    overlay.getElement().getStyle().setProperty("justifyContent", "center");

    // Modal card
    FlowPanel card = new FlowPanel();
    card.getElement().getStyle().setProperty("background", Theme.COLOR_PANEL);
    card.getElement().getStyle().setProperty("borderRadius", "14px");
    card.getElement().getStyle().setProperty("padding", "28px 32px");
    card.getElement().getStyle().setProperty("minWidth", "380px");
    card.getElement().getStyle().setProperty("maxWidth", "520px");
    card.getElement().getStyle().setProperty("boxShadow", "0 20px 60px rgba(0,0,0,0.3)");
    card.getElement().getStyle().setProperty("display", "grid");
    card.getElement().getStyle().setProperty("gap", "16px");

    // Title row
    FlowPanel titleRow = new FlowPanel();
    titleRow.getElement().getStyle().setProperty("display", "flex");
    titleRow.getElement().getStyle().setProperty("alignItems", "center");
    titleRow.getElement().getStyle().setProperty("justifyContent", "space-between");
    Label title = new Label("▶ Run Report");
    title.getElement().getStyle().setProperty("fontWeight", "700");
    title.getElement().getStyle().setProperty("fontSize", "15px");
    title.getElement().getStyle().setProperty("color", Theme.COLOR_TEXT);
    Button closeBtn = UiFactory.ghostButton("✕");
    closeBtn.addClickHandler(e -> root.remove(overlay));
    titleRow.add(title);
    titleRow.add(closeBtn);
    card.add(titleRow);

    // Executed at
    Label atLabel = new Label("Executed: " + execAt);
    atLabel.getElement().getStyle().setProperty("fontSize", Theme.FONT_SIZE_78);
    atLabel.getElement().getStyle().setProperty("color", Theme.COLOR_MUTED);
    card.add(atLabel);

    // Stats row
    FlowPanel statsRow = new FlowPanel();
    statsRow.getElement().getStyle().setProperty("display", "grid");
    statsRow.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(4,1fr)");
    statsRow.getElement().getStyle().setProperty("gap", "8px");
    statsRow.add(statBadge("Total", String.valueOf(total), "#2563eb", "#eff6ff"));
    statsRow.add(statBadge("Passed", String.valueOf(passed), "#16a34a", "#dcfce7"));
    statsRow.add(statBadge("Failed", String.valueOf(failed), failed > 0 ? "#dc2626" : "#16a34a", failed > 0 ? "#fee2e2" : "#dcfce7"));
    statsRow.add(statBadge("Duration", ms + " ms", "#7c3aed", "#f5f3ff"));
    card.add(statsRow);

    // Step list summary
    JSONArray steps = obj.get("steps").isArray();
    if (steps != null) {
        FlowPanel stepList = new FlowPanel();
        stepList.getElement().getStyle().setProperty("display", "grid");
        stepList.getElement().getStyle().setProperty("gap", "4px");
        stepList.getElement().getStyle().setProperty("maxHeight", "220px");
        stepList.getElement().getStyle().setProperty("overflowY", "auto");
        for (int i = 0; i < steps.size(); i++) {
            JSONObject s = steps.get(i).isObject();
            if (s == null) continue;
            boolean stepPassed = s.get("passed").isBoolean().booleanValue();
            String sid = s.get("stepIdentifier").isString().stringValue();
            long sdur = (long) s.get("durationMs").isNumber().doubleValue();
            int scode = (int) s.get("statusCode").isNumber().doubleValue();

            FlowPanel row = new FlowPanel();
            row.getElement().getStyle().setProperty("display", "flex");
            row.getElement().getStyle().setProperty("alignItems", "center");
            row.getElement().getStyle().setProperty("gap", "8px");
            row.getElement().getStyle().setProperty("padding", "5px 8px");
            row.getElement().getStyle().setProperty("borderRadius", "6px");
            row.getElement().getStyle().setProperty("background", stepPassed ? "#f0fdf4" : "#fef2f2");

            Label icon = new Label(stepPassed ? "✓" : "✗");
            icon.getElement().getStyle().setProperty("color", stepPassed ? "#16a34a" : "#dc2626");
            icon.getElement().getStyle().setProperty("fontWeight", "700");
            icon.getElement().getStyle().setProperty("minWidth", "16px");
            icon.getElement().getStyle().setProperty("fontSize", "12px");

            Label name = new Label(sid);
            name.getElement().getStyle().setProperty("flex", "1");
            name.getElement().getStyle().setProperty("fontSize", "12px");
            name.getElement().getStyle().setProperty("color", Theme.COLOR_TEXT);

            Label meta = new Label(scode + "  " + sdur + " ms");
            meta.getElement().getStyle().setProperty("fontSize", "11px");
            meta.getElement().getStyle().setProperty("color", Theme.COLOR_MUTED);
            meta.getElement().getStyle().setProperty("fontFamily", "monospace");

            row.add(icon);
            row.add(name);
            row.add(meta);
            stepList.add(row);
        }
        card.add(stepList);
    }

    // View Report button
    Button viewBtn = UiFactory.outlineButton("View Full Report ↗");
    viewBtn.getElement().getStyle().setProperty("cursor", "pointer");
    viewBtn.addClickHandler(e -> openReportInNewTab(reportUrl));
    card.add(viewBtn);

    overlay.add(card);
    root.add(overlay);
}

private FlowPanel statBadge(String label, String value, String color, String bg) {
    FlowPanel p = new FlowPanel();
    p.getElement().getStyle().setProperty("background", bg);
    p.getElement().getStyle().setProperty("borderRadius", "8px");
    p.getElement().getStyle().setProperty("padding", "10px 8px");
    p.getElement().getStyle().setProperty("textAlign", "center");
    Label v = new Label(value);
    v.getElement().getStyle().setProperty("fontWeight", "700");
    v.getElement().getStyle().setProperty("color", color);
    v.getElement().getStyle().setProperty("fontSize", "18px");
    Label l = new Label(label);
    l.getElement().getStyle().setProperty("fontSize", "10px");
    l.getElement().getStyle().setProperty("color", color);
    p.add(v); p.add(l);
    return p;
}

private static native void openReportInNewTab(String url) /*-{
    $wnd.open(url, '_blank');
}-*/;
```

- [ ] **Step 19: Build**

```bash
mvn -q -DskipTests clean package
```
Expected: no output (clean build)

- [ ] **Step 20: End-to-end test**

1. Start server (already running on port 8000, restart if needed):
   ```bash
   mvn -q -DskipTests exec:java &
   ```
2. Open browser at http://localhost:8000
3. Load the `auth-pharmacy-upload-prescription.flow.meta` flow
4. Click "▶ Run Collection" in navbar
5. Verify: button shows "⏳ Running…" while in flight
6. Verify: result overlay shows Total/Passed/Failed/Duration stats + per-step rows
7. Click "View Full Report ↗" — verify HTML report opens in new tab with step accordions

---

## Chunk 3: Theme constant check

### Task 6: Ensure Theme.COLOR_TEMPLATE exists

**Files:**
- Check: `src/main/java/com/visit/gwtapiflowbuilder/client/theme/Theme.java`

- [ ] **Step 21: Check COLOR_TEMPLATE exists in Theme.java**

If it doesn't exist, use `Theme.COLOR_ACCENT` or `"#2563eb"` as the fallback in NavbarView for the run button color.

---

## Notes for implementor

- `FlowRunner.java` has no GWT dependencies — it's pure Java server-side
- Multipart steps (STEP_3_ADD_MULTIPLE_FILES) will show `[multipart — skipped]` as body and the status check will likely fail if the server actually requires the file — this is by design for now
- The `openReportInNewTab` uses JSNI (`$wnd.open`) which is the correct approach for browser window operations — there is no pure Java equivalent in GWT
- If `addCors` is not static in FlowServer (it is), call it directly; FlowRunHandler is an inner static class and can call static methods of FlowServer
- The HTML report is saved to the same `site/` dir the static handler serves — so `reportUrl` like `/report-run-xxx.html` is immediately accessible
