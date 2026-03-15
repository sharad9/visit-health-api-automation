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

    public void runStep(StepBlock block, Button runButton) {
        if (block == null) {
            return;
        }
        if (runButton != null) {
            runButton.setText("Running…");
            runButton.setEnabled(false);
        }
        String method = selectedText(block.method);
        if (method == null || method.trim().isEmpty()) {
            method = "GET";
        }
        String resolvedUrl = tokenRenderer.replaceTokens(block.url == null ? "" : block.url.getValue());
        if (resolvedUrl.trim().isEmpty()) {
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

        JSONObject requestHeadersObject = new JSONObject();
        boolean hasContentType = false;
        for (KeyValueRow row : block.headers) {
            String key = row.key.getValue();
            if (key == null) continue;
            key = key.trim();
            if (key.isEmpty()) continue;
            String value = tokenRenderer.replaceTokens(row.value.getValue());
            requestHeadersObject.put(key, new JSONString(value));
            if ("content-type".equalsIgnoreCase(key)) {
                hasContentType = true;
            }
        }

        JSONObject requestBodyObject = new JSONObject();
        int bodyFieldCount = 0;
        for (KeyValueRow row : block.body) {
            String key = row.key.getValue();
            if (key == null) continue;
            key = key.trim();
            if (key.isEmpty()) continue;
            String value = tokenRenderer.replaceTokens(row.value.getValue());
            requestBodyObject.put(key, new JSONString(value));
            bodyFieldCount++;
        }
        String serializedBody = null;
        if (bodyFieldCount > 0) {
            serializedBody = requestBodyObject.toString();
            if (!hasContentType) {
                requestHeadersObject.put("Content-Type", new JSONString("application/json"));
            }
        }

        JSONObject payload = new JSONObject();
        payload.put("method", new JSONString(httpMethod.toString()));
        payload.put("url", new JSONString(resolvedUrl));
        payload.put("headers", requestHeadersObject);
        if (serializedBody != null) {
            payload.put("body", new JSONString(serializedBody));
        }

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, PROXY_ENDPOINT);
        builder.setHeader("Content-Type", "application/json");

        final long requestStartTimeMs = System.currentTimeMillis();
        final String startDateTime = getNativeDateTime();
        final String finalMethod = method;

        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    long durationMs = System.currentTimeMillis() - requestStartTimeMs;
                    if (runButton != null) {
                        runButton.setText("▶ Run");
                        runButton.setEnabled(true);
                    }
                    String responseText = response.getText();
                    if (responseText == null || responseText.trim().isEmpty()) {
                        responseText = "";
                    }
                    JSONObject responseObject;
                    try {
                        responseObject = JSONParser.parseStrict(responseText).isObject();
                    } catch (Exception parseException) {
                        responseObject = null;
                    }
                    int statusCode = response.getStatusCode();
                    String responseHeaders = response.getHeadersAsString();
                    String responseBody = responseText;
                    if (responseObject != null) {
                        statusCode = (int) FlowParser.numberValueOrDefault(responseObject.get("status"), statusCode);
                        responseHeaders = FlowParser.stringValue(responseObject.get("headers"), "");
                        responseBody = FlowParser.stringValue(responseObject.get("body"), "");
                    }
                    String stepId = block.stepId == null ? "" : block.stepId.getValue().trim();
                    extractResponseVariables(stepId, responseBody, responseHeaders, block);
                    if (block.runResponse != null) {
                        block.runResponse.setText(formatRunResponse(statusCode, responseHeaders, responseBody, durationMs, startDateTime));
                    } else {
                        Window.alert("Step Run: " + response.getStatusCode() + "\n" + responseText);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    if (runButton != null) {
                        runButton.setText("▶ Run");
                        runButton.setEnabled(true);
                    }
                    String message = "Step Run Failed: " + (exception == null ? "Unknown error" : exception.getMessage());
                    if (block.runResponse != null) {
                        block.runResponse.setText(message);
                    } else {
                        Window.alert(message);
                    }
                }
            });
        } catch (RequestException requestException) {
            if (runButton != null) {
                runButton.setText("▶ Run");
                runButton.setEnabled(true);
            }
            String message = "Step Run Failed: " + requestException.getMessage();
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

    public void extractResponseVariables(String stepId, String responseBody, String responseHeaders, StepBlock block) {
        if (stepId == null || stepId.isEmpty()) {
            return;
        }
        JSONObject parsedResponseBody = null;
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                JSONValue parsed = JSONParser.parseStrict(responseBody);
                parsedResponseBody = parsed.isObject();
            } catch (Exception parseException) {
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
            String extracted = resolveJsonPath(parsedResponseBody, jsonPath);
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
            String extracted = extractHeaderValue(responseHeaders, headerName);
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
            int bracketIndex = part.indexOf('[');
            if (bracketIndex >= 0) {
                int closeBracketIndex = part.indexOf(']', bracketIndex);
                if (closeBracketIndex < 0) return null;
                String key = part.substring(0, bracketIndex);
                String arrayIndexString = part.substring(bracketIndex + 1, closeBracketIndex);
                int arrayElementIndex;
                try {
                    arrayElementIndex = Integer.parseInt(arrayIndexString);
                } catch (NumberFormatException numberFormatException) {
                    return null;
                }
                if (!key.isEmpty()) {
                    JSONObject jsonObject = current.isObject();
                    if (jsonObject == null) return null;
                    current = jsonObject.get(key);
                }
                JSONArray jsonArray = current == null ? null : current.isArray();
                if (jsonArray == null) return null;
                current = jsonArray.get(arrayElementIndex);
            } else {
                JSONObject jsonObject = current.isObject();
                if (jsonObject == null) return null;
                current = jsonObject.get(part);
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
            int colonIndex = line.indexOf(':');
            if (colonIndex < 0) continue;
            String key = line.substring(0, colonIndex).trim();
            if (key.equalsIgnoreCase(headerName)) {
                return line.substring(colonIndex + 1).trim();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Response formatting
    // -------------------------------------------------------------------------

    public String formatRunResponse(int statusCode, String headers, String body, long durationMs, String executedAt) {
        StringBuilder formattedResponse = new StringBuilder();
        formattedResponse.append("Executed At: ").append(executedAt)
          .append("  |  Duration: ").append(durationMs).append(" ms")
          .append("  |  Status: ").append(statusCode).append("\n\n");
        formattedResponse.append("Headers:\n");
        if (headers == null || headers.trim().isEmpty()) {
            formattedResponse.append("(none)");
        } else {
            formattedResponse.append(headers.trim());
        }
        formattedResponse.append("\n\nBody:\n");
        if (body == null || body.isEmpty()) {
            formattedResponse.append("(empty)");
        } else {
            formattedResponse.append(body);
        }
        return formattedResponse.toString();
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
        int selectedIndex = listBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= listBox.getItemCount()) return "";
        return listBox.getItemText(selectedIndex);
    }
}
