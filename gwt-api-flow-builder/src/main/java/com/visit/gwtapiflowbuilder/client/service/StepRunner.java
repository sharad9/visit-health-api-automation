package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.ui.KeyValueRow;
import com.visit.gwtapiflowbuilder.client.ui.StepBlock;
import com.visit.gwtapiflowbuilder.client.ui.TokenRenderer;

/**
 * Handles per-step HTTP execution via the /api/proxy endpoint.
 * Extracts response variables into AppState.runtimeVariables after each run.
 */
public final class StepRunner {

    private static final String PROXY_ENDPOINT = "/api/proxy";

    private final AppState state;
    private final TokenRenderer tokenRenderer;

    public StepRunner(AppState state, TokenRenderer tokenRenderer) {
        this.state = state;
        this.tokenRenderer = tokenRenderer;
    }

    // -------------------------------------------------------------------------
    // Run a single step
    // -------------------------------------------------------------------------

    public void runStep(StepBlock block, Button runBtn) {
        if (block == null) {
            return;
        }
        if (runBtn != null) {
            runBtn.setText("Running…");
            runBtn.setEnabled(false);
        }
        String method = selectedText(block.method);
        if (method == null || method.trim().isEmpty()) {
            method = "GET";
        }
        String url = tokenRenderer.replaceTokens(block.url == null ? "" : block.url.getValue());
        if (url.trim().isEmpty()) {
            Window.alert("Request URL is empty.");
            return;
        }
        RequestBuilder.Method httpMethod;
        if ("POST".equalsIgnoreCase(method)) {
            httpMethod = RequestBuilder.POST;
        } else if ("PUT".equalsIgnoreCase(method)) {
            httpMethod = RequestBuilder.PUT;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            httpMethod = RequestBuilder.DELETE;
        } else {
            httpMethod = RequestBuilder.GET;
        }

        JSONObject headersObj = new JSONObject();
        boolean hasContentType = false;
        for (KeyValueRow row : block.headers) {
            String key = row.key.getValue();
            if (key == null) continue;
            key = key.trim();
            if (key.isEmpty()) continue;
            String value = tokenRenderer.replaceTokens(row.value.getValue());
            headersObj.put(key, new JSONString(value));
            if ("content-type".equalsIgnoreCase(key)) {
                hasContentType = true;
            }
        }

        JSONObject bodyObj = new JSONObject();
        int bodyCount = 0;
        for (KeyValueRow row : block.body) {
            String key = row.key.getValue();
            if (key == null) continue;
            key = key.trim();
            if (key.isEmpty()) continue;
            String value = tokenRenderer.replaceTokens(row.value.getValue());
            bodyObj.put(key, new JSONString(value));
            bodyCount++;
        }
        String bodyStr = null;
        if (bodyCount > 0) {
            bodyStr = bodyObj.toString();
            if (!hasContentType) {
                headersObj.put("Content-Type", new JSONString("application/json"));
            }
        }

        JSONObject payload = new JSONObject();
        payload.put("method", new JSONString(httpMethod.toString()));
        payload.put("url", new JSONString(url));
        payload.put("headers", headersObj);
        if (bodyStr != null) {
            payload.put("body", new JSONString(bodyStr));
        }

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, PROXY_ENDPOINT);
        builder.setHeader("Content-Type", "application/json");

        final long startMs = System.currentTimeMillis();
        final String startDateTime = getNativeDateTime();
        final String finalMethod = method;

        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    long durationMs = System.currentTimeMillis() - startMs;
                    if (runBtn != null) {
                        runBtn.setText("▶ Run");
                        runBtn.setEnabled(true);
                    }
                    String text = response.getText();
                    if (text == null || text.trim().isEmpty()) {
                        text = "";
                    }
                    JSONObject obj;
                    try {
                        obj = JSONParser.parseStrict(text).isObject();
                    } catch (Exception ex) {
                        obj = null;
                    }
                    int status = response.getStatusCode();
                    String headers = response.getHeadersAsString();
                    String body = text;
                    if (obj != null) {
                        status = (int) FlowParser.numberValueOrDefault(obj.get("status"), status);
                        headers = FlowParser.stringValue(obj.get("headers"), "");
                        body = FlowParser.stringValue(obj.get("body"), "");
                    }
                    String stepId = block.stepId == null ? "" : block.stepId.getValue().trim();
                    extractResponseVariables(stepId, body, headers, block);
                    if (block.runResponse != null) {
                        block.runResponse.setText(formatRunResponse(status, headers, body, durationMs, startDateTime));
                    } else {
                        Window.alert("Step Run: " + response.getStatusCode() + "\n" + text);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    if (runBtn != null) {
                        runBtn.setText("▶ Run");
                        runBtn.setEnabled(true);
                    }
                    String message = "Step Run Failed: " + (exception == null ? "Unknown error" : exception.getMessage());
                    if (block.runResponse != null) {
                        block.runResponse.setText(message);
                    } else {
                        Window.alert(message);
                    }
                }
            });
        } catch (RequestException ex) {
            if (runBtn != null) {
                runBtn.setText("▶ Run");
                runBtn.setEnabled(true);
            }
            String message = "Step Run Failed: " + ex.getMessage();
            if (block.runResponse != null) {
                block.runResponse.setText(message);
            } else {
                Window.alert(message);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Variable extraction
    // -------------------------------------------------------------------------

    public void extractResponseVariables(String stepId, String body, String headers, StepBlock block) {
        if (stepId == null || stepId.isEmpty()) {
            return;
        }
        JSONObject bodyObj = null;
        if (body != null && !body.isEmpty()) {
            try {
                JSONValue parsed = JSONParser.parseStrict(body);
                bodyObj = parsed.isObject();
            } catch (Exception e) {
                // not JSON, skip body extractions
            }
        }
        for (KeyValueRow row : block.extractBody) {
            String varName = row.key.getValue();
            String jsonPath = row.value.getValue();
            if (varName == null || varName.trim().isEmpty() || jsonPath == null || jsonPath.trim().isEmpty()) {
                continue;
            }
            varName = varName.trim();
            jsonPath = jsonPath.trim();
            String extracted = resolveJsonPath(bodyObj, jsonPath);
            if (extracted != null) {
                state.runtimeVariables.put(stepId + "_REQUEST_BODY_" + varName, extracted);
            }
        }
        for (KeyValueRow row : block.extractHeaders) {
            String varName = row.key.getValue();
            String headerName = row.value.getValue();
            if (varName == null || varName.trim().isEmpty() || headerName == null || headerName.trim().isEmpty()) {
                continue;
            }
            varName = varName.trim();
            headerName = headerName.trim();
            String extracted = extractHeaderValue(headers, headerName);
            if (extracted != null) {
                state.runtimeVariables.put(stepId + "_REQUEST_HEADER_" + varName, extracted);
            }
        }
    }

    public String resolveJsonPath(JSONObject root, String path) {
        if (root == null || path == null) {
            return null;
        }
        if (path.startsWith("$.")) {
            path = path.substring(2);
        } else if (path.startsWith("$")) {
            path = path.substring(1);
        }
        if (path.isEmpty()) {
            return root.toString();
        }
        String[] parts = path.split("\\.");
        JSONValue current = root;
        for (String part : parts) {
            if (current == null) return null;
            int bracketIdx = part.indexOf('[');
            if (bracketIdx >= 0) {
                int closeBracket = part.indexOf(']', bracketIdx);
                if (closeBracket < 0) return null;
                String key = part.substring(0, bracketIdx);
                String indexStr = part.substring(bracketIdx + 1, closeBracket);
                int index;
                try {
                    index = Integer.parseInt(indexStr);
                } catch (NumberFormatException e) {
                    return null;
                }
                if (!key.isEmpty()) {
                    JSONObject obj = current.isObject();
                    if (obj == null) return null;
                    current = obj.get(key);
                }
                JSONArray arr = current == null ? null : current.isArray();
                if (arr == null) return null;
                current = arr.get(index);
            } else {
                JSONObject obj = current.isObject();
                if (obj == null) return null;
                current = obj.get(part);
            }
        }
        if (current == null) return null;
        JSONString str = current.isString();
        if (str != null) return str.stringValue();
        JSONNumber num = current.isNumber();
        if (num != null) {
            double d = num.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        JSONBoolean bool = current.isBoolean();
        if (bool != null) return String.valueOf(bool.booleanValue());
        if (current.isNull() != null) return "null";
        return current.toString();
    }

    public String extractHeaderValue(String headersText, String headerName) {
        if (headersText == null || headersText.isEmpty() || headerName == null) {
            return null;
        }
        String[] lines = headersText.split("\n");
        for (String line : lines) {
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String key = line.substring(0, colon).trim();
            if (key.equalsIgnoreCase(headerName)) {
                return line.substring(colon + 1).trim();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Response formatting
    // -------------------------------------------------------------------------

    public String formatRunResponse(int statusCode, String headers, String body, long durationMs, String executedAt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Executed At: ").append(executedAt)
          .append("  |  Duration: ").append(durationMs).append(" ms")
          .append("  |  Status: ").append(statusCode).append("\n\n");
        sb.append("Headers:\n");
        if (headers == null || headers.trim().isEmpty()) {
            sb.append("(none)");
        } else {
            sb.append(headers.trim());
        }
        sb.append("\n\nBody:\n");
        if (body == null || body.isEmpty()) {
            sb.append("(empty)");
        } else {
            sb.append(body);
        }
        return sb.toString();
    }

    public static native String getNativeDateTime() /*-{
        var d = new Date();
        var p = function(n) { return n < 10 ? '0' + n : '' + n; };
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
            + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
    }-*/;

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String selectedText(com.google.gwt.user.client.ui.ListBox listBox) {
        if (listBox == null) return "";
        int idx = listBox.getSelectedIndex();
        if (idx < 0 || idx >= listBox.getItemCount()) return "";
        return listBox.getItemText(idx);
    }
}
