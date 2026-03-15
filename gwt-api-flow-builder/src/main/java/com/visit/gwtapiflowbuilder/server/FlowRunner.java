package com.visit.gwtapiflowbuilder.server;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public final class FlowRunner {

    // -------------------------------------------------------------------------
    // Token substitution
    // -------------------------------------------------------------------------

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
        merged.putAll(runtimeVars);
        return merged;
    }

    // -------------------------------------------------------------------------
    // JSONPath (subset: $.a, $.a.b, $.a[0].b)
    // -------------------------------------------------------------------------

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
                    int idx = Integer.parseInt(
                        part.substring(part.indexOf('[') + 1, part.indexOf(']')));
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

    // -------------------------------------------------------------------------
    // Header extraction (parses "key: value\n" raw header text)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Data classes
    // -------------------------------------------------------------------------

    public static final class CheckResult {
        public final String source;
        public final String detail;
        public final boolean passed;

        CheckResult(String source, boolean passed, String detail) {
            this.source = source;
            this.passed = passed;
            this.detail = detail;
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

    // -------------------------------------------------------------------------
    // Main entry point
    // -------------------------------------------------------------------------

    public static RunReport runFlow(String flowName, JsonObject flow) {
        Map<String, String> globalInputs = new LinkedHashMap<>();
        JsonObject gi = flow.has("globalInputs") ? flow.getAsJsonObject("globalInputs") : null;
        if (gi != null) {
            for (String k : gi.keySet()) {
                JsonElement v = gi.get(k);
                globalInputs.put(k, v.isJsonPrimitive() ? v.getAsString() : v.toString());
            }
        }

        Map<String, String> envVars = new LinkedHashMap<>();
        JsonObject environments = flow.has("environments")
            ? flow.getAsJsonObject("environments") : null;
        if (environments != null) {
            String activeId = environments.has("activeEnvironmentId")
                ? environments.get("activeEnvironmentId").getAsString() : null;
            JsonArray items = environments.has("environmentItems")
                ? environments.getAsJsonArray("environmentItems") : new JsonArray();
            for (JsonElement el : items) {
                JsonObject env = el.getAsJsonObject();
                String id = env.has("environmentId")
                    ? env.get("environmentId").getAsString() : null;
                if (Objects.equals(id, activeId) && env.has("environmentVariables")) {
                    JsonObject evs = env.getAsJsonObject("environmentVariables");
                    for (String k : evs.keySet()) {
                        JsonElement v = evs.get(k);
                        envVars.put(k, v.isJsonPrimitive() ? v.getAsString() : v.toString());
                    }
                    break;
                }
            }
        }

        Map<String, String> runtimeVars = new LinkedHashMap<>();
        List<StepResult> results = new ArrayList<>();
        long flowStart = System.currentTimeMillis();

        JsonArray steps = flow.has("steps") ? flow.getAsJsonArray("steps") : new JsonArray();
        for (JsonElement stepEl : steps) {
            StepResult result = executeStep(
                stepEl.getAsJsonObject(), globalInputs, envVars, runtimeVars);
            results.add(result);
            runtimeVars.putAll(result.extractedVariables);
        }

        long totalMs = System.currentTimeMillis() - flowStart;
        return new RunReport(flowName, nowFormatted(), totalMs, results);
    }

    // -------------------------------------------------------------------------
    // Step execution
    // -------------------------------------------------------------------------

    private static StepResult executeStep(JsonObject step,
                                           Map<String, String> globalInputs,
                                           Map<String, String> envVars,
                                           Map<String, String> runtimeVars) {
        String stepId = step.has("stepIdentifier")
            ? step.get("stepIdentifier").getAsString() : "UNKNOWN";
        JsonObject request = step.has("request")
            ? step.getAsJsonObject("request") : new JsonObject();

        Map<String, String> vars = mergedVars(globalInputs, envVars, runtimeVars);

        String method = request.has("httpMethod")
            ? request.get("httpMethod").getAsString() : "GET";
        String url = substitute(
            request.has("requestUrl") ? request.get("requestUrl").getAsString() : "", vars);
        int timeoutMs = request.has("timeoutMilliseconds")
            ? request.get("timeoutMilliseconds").getAsInt() : 10000;

        Map<String, String> headers = new LinkedHashMap<>();
        if (request.has("headers") && request.get("headers").isJsonObject()) {
            JsonObject h = request.getAsJsonObject("headers");
            for (String k : h.keySet()) {
                JsonElement v = h.get(k);
                headers.put(k, substitute(v.isJsonPrimitive() ? v.getAsString() : v.toString(), vars));
            }
        }

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
            java.net.HttpURLConnection conn =
                (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs * 2);
            conn.setRequestMethod(method.toUpperCase());

            for (Map.Entry<String, String> e : headers.entrySet()) {
                String k = e.getKey().toLowerCase();
                if ("host".equals(k) || "content-length".equals(k)) continue;
                conn.setRequestProperty(e.getKey(), e.getValue());
            }

            boolean hasBody = !bodyStr.isEmpty() && !"[multipart — skipped]".equals(bodyStr)
                && !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method);
            if (hasBody) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(bodyStr.getBytes(StandardCharsets.UTF_8));
            }

            statusCode = conn.getResponseCode();
            InputStream stream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            responseBody = stream == null ? "" : readStream(stream);
            responseHeaders = formatHeaderMap(conn.getHeaderFields());
            conn.disconnect();
        } catch (Exception ex) {
            errorMessage = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }

        long durationMs = System.currentTimeMillis() - start;

        Map<String, String> extracted =
            extractVariables(stepId, step, responseBody, responseHeaders);
        List<CheckResult> checkResults =
            evaluateChecks(step, statusCode, responseBody, responseHeaders);
        boolean passed = errorMessage == null && checkResults.stream().allMatch(c -> c.passed);

        return new StepResult(stepId, method, url, headers, bodyStr,
                              statusCode, responseHeaders, responseBody,
                              durationMs, extracted, checkResults, passed, errorMessage);
    }

    // -------------------------------------------------------------------------
    // Variable extraction
    // -------------------------------------------------------------------------

    private static Map<String, String> extractVariables(String stepId, JsonObject step,
                                                         String responseBody,
                                                         String responseHeaders) {
        Map<String, String> vars = new LinkedHashMap<>();
        if (!step.has("extraction")) return vars;
        JsonObject extraction = step.getAsJsonObject("extraction");

        if (extraction.has("bodyJsonPaths") && extraction.get("bodyJsonPaths").isJsonObject()) {
            JsonObject paths = extraction.getAsJsonObject("bodyJsonPaths");
            for (String varName : paths.keySet()) {
                String path = paths.get(varName).getAsString();
                String value = resolveJsonPath(responseBody, path);
                if (value != null) {
                    vars.put(stepId + "_REQUEST_BODY_" + varName, value);
                }
            }
        }

        if (extraction.has("headerValues") && extraction.get("headerValues").isJsonObject()) {
            JsonObject hvs = extraction.getAsJsonObject("headerValues");
            for (String varName : hvs.keySet()) {
                String headerName = hvs.get(varName).getAsString();
                String value = extractHeaderValue(responseHeaders, headerName);
                if (value != null) {
                    vars.put(stepId + "_REQUEST_HEADER_" + varName, value);
                }
            }
        }
        return vars;
    }

    // -------------------------------------------------------------------------
    // Check evaluation
    // -------------------------------------------------------------------------

    private static List<CheckResult> evaluateChecks(JsonObject step, int statusCode,
                                                      String responseBody,
                                                      String responseHeaders) {
        List<CheckResult> results = new ArrayList<>();
        if (!step.has("checks")) return results;
        for (JsonElement el : step.getAsJsonArray("checks")) {
            JsonObject check = el.getAsJsonObject();
            String source = check.has("source") ? check.get("source").getAsString() : "";
            switch (source) {
                case "status": {
                    boolean ok = statusCode >= 200 && statusCode < 300;
                    results.add(new CheckResult("status", ok,
                        ok ? "Status " + statusCode + " OK" : "Status " + statusCode + " FAILED"));
                    break;
                }
                case "body": {
                    if (check.has("jsonPath")) {
                        String path = check.get("jsonPath").getAsString();
                        String val = resolveJsonPath(responseBody, path);
                        if (check.has("equals")) {
                            String expected = check.get("equals").getAsString();
                            boolean eq = expected.equals(val);
                            results.add(new CheckResult("body", eq,
                                path + (eq ? " == " : " != ") + expected));
                        } else {
                            boolean ex = val != null;
                            results.add(new CheckResult("body", ex,
                                path + (ex ? " exists" : " not found")));
                        }
                    } else {
                        boolean ok = responseBody != null && !responseBody.isBlank();
                        results.add(new CheckResult("body", ok,
                            ok ? "Body non-empty" : "Body empty"));
                    }
                    break;
                }
                default:
                    results.add(new CheckResult(source, true, "skipped"));
            }
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // JSON substitution helpers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // I/O helpers
    // -------------------------------------------------------------------------

    private static String readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = stream.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString(StandardCharsets.UTF_8);
    }

    private static String formatHeaderMap(Map<String, List<String>> headers) {
        if (headers == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey() == null) continue;
            for (String v : e.getValue()) {
                sb.append(e.getKey()).append(": ").append(v).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String nowFormatted() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond());
    }
}
