package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.ListBox;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.ui.CheckRow;
import com.visit.gwtapiflowbuilder.client.ui.KeyValueRow;
import com.visit.gwtapiflowbuilder.client.ui.StepBlock;

import java.util.ArrayList;
import java.util.List;

public final class FlowJsonBuilder {
    private FlowJsonBuilder() {
    }

    public static String prettyPrint(JSONValue value) {
        return JsonPrettyPrinter.prettyPrint(value);
    }

    public static JSONObject build(String metaId,
                                   String metaVersion,
                                   List<EnvironmentItem> environments,
                                   int activeEnvIndex,
                                   List<KeyValueRow> globalInputRows,
                                   List<StepBlock> steps) {
        JSONObject root = new JSONObject();
        JSONObject metadata = new JSONObject();
        metadata.put("identifier", jsonString(metaId));
        metadata.put("version", jsonString(metaVersion));
        root.put("metadata", metadata);

        EnvironmentItem active = environments.get(activeEnvIndex);
        JSONObject environment = new JSONObject();
        if (notEmpty(active.baseUrl)) {
            environment.put("baseUrl", jsonString(active.baseUrl));
        }
        environment.put("defaults", defaultsObject(active.timeoutMs, active.retryCount, active.retryDelay));
        root.put("environment", environment);

        JSONObject environmentsJson = new JSONObject();
        environmentsJson.put("activeEnvironmentId", jsonString(active.id));
        JSONArray envItems = new JSONArray();
        for (int i = 0; i < environments.size(); i++) {
            EnvironmentItem env = environments.get(i);
            JSONObject envJson = new JSONObject();
            envJson.put("environmentId", jsonString(env.id));
            envJson.put("environmentName", jsonString(env.name));
            if (notEmpty(env.baseUrl)) {
                envJson.put("baseUrl", jsonString(env.baseUrl));
            }
            envJson.put("defaults", defaultsObject(env.timeoutMs, env.retryCount, env.retryDelay));
            JSONObject envVars = toObject(env.variables);
            if (envVars.size() > 0) {
                envJson.put("environmentVariables", envVars);
            }
            envItems.set(i, envJson);
        }
        environmentsJson.put("environmentItems", envItems);
        root.put("environments", environmentsJson);

        JSONObject globalInputs = toObject(toPairs(globalInputRows));
        if (globalInputs.size() > 0) {
            root.put("globalInputs", globalInputs);
        }

        JSONArray stepsJson = new JSONArray();
        for (int i = 0; i < steps.size(); i++) {
            StepBlock step = steps.get(i);
            JSONObject stepJson = new JSONObject();
            stepJson.put("stepIdentifier", jsonString(step.stepId.getValue()));

            JSONObject request = new JSONObject();
            request.put("httpMethod", jsonString(selectedText(step.method)));
            request.put("requestUrl", jsonString(step.url.getValue()));
            if (notEmpty(step.timeoutMs.getValue())) {
                request.put("timeoutMilliseconds", jsonNumber(step.timeoutMs.getValue()));
            }
            if (notEmpty(step.retryCount.getValue())) {
                JSONObject retry = new JSONObject();
                retry.put("maximumAttempts", jsonNumber(step.retryCount.getValue()));
                retry.put("delayMilliseconds", jsonNumber(step.retryDelay.getValue()));
                request.put("retryPolicy", retry);
            }

            JSONObject headers = toObject(toPairs(step.headers));
            if (headers.size() > 0) {
                request.put("headers", headers);
            }
            JSONObject reqVars = toObject(toPairs(step.requestVariables));
            if (reqVars.size() > 0) {
                request.put("requestVariables", reqVars);
            }
            JSONObject body = toObjectParsed(step.body);
            if (body.size() > 0) {
                request.put("body", body);
            }
            stepJson.put("request", request);

            JSONObject extraction = new JSONObject();
            JSONObject extractBody = toObject(toPairs(step.extractBody));
            JSONObject extractHeaders = toObject(toPairs(step.extractHeaders));
            if (extractBody.size() > 0) {
                extraction.put("bodyJsonPaths", extractBody);
            }
            if (extractHeaders.size() > 0) {
                extraction.put("headerValues", extractHeaders);
            }
            if (extraction.size() > 0) {
                stepJson.put("extraction", extraction);
            }

            JSONArray checks = toChecks(step.checks);
            if (checks.size() > 0) {
                stepJson.put("checks", checks);
            }

            stepsJson.set(i, stepJson);
        }
        root.put("steps", stepsJson);

        return root;
    }

    private static JSONObject defaultsObject(String timeout, String count, String delay) {
        JSONObject defaults = new JSONObject();
        defaults.put("timeoutMilliseconds", jsonNumber(timeout));
        JSONObject retry = new JSONObject();
        retry.put("maximumAttempts", jsonNumber(count));
        retry.put("delayMilliseconds", jsonNumber(delay));
        defaults.put("retryPolicy", retry);
        return defaults;
    }

    private static JSONObject toObject(List<KeyValuePair> pairs) {
        JSONObject obj = new JSONObject();
        for (KeyValuePair pair : pairs) {
            if (notEmpty(pair.key)) {
                obj.put(pair.key, jsonString(pair.value));
            }
        }
        return obj;
    }

    private static JSONObject toObjectParsed(List<KeyValueRow> rows) {
        JSONObject obj = new JSONObject();
        for (KeyValueRow row : rows) {
            String key = row.key.getValue();
            if (notEmpty(key)) {
                obj.put(key, parseValue(row.value.getValue()));
            }
        }
        return obj;
    }

    private static JSONArray toChecks(List<CheckRow> rows) {
        JSONArray arr = new JSONArray();
        int index = 0;
        for (CheckRow row : rows) {
            String source = selectedText(row.source);
            if (!notEmpty(source)) {
                continue;
            }
            JSONObject check = new JSONObject();
            check.put("source", jsonString(source));
            if (notEmpty(row.path.getValue())) {
                check.put("jsonPath", jsonString(row.path.getValue()));
            }
            if (notEmpty(row.equals.getValue())) {
                check.put("equals", parseValue(row.equals.getValue()));
            }
            if (row.exists.getValue()) {
                check.put("exists", JSONBoolean.getInstance(true));
            }
            arr.set(index++, check);
        }
        return arr;
    }

    private static List<KeyValuePair> toPairs(List<KeyValueRow> rows) {
        List<KeyValuePair> pairs = new ArrayList<>();
        for (KeyValueRow row : rows) {
            pairs.add(new KeyValuePair(row.key.getValue(), row.value.getValue()));
        }
        return pairs;
    }

    private static JSONValue parseValue(String value) {
        if (value == null) {
            return new JSONString("");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return new JSONString("");
        }
        if ("true".equalsIgnoreCase(trimmed)) {
            return JSONBoolean.getInstance(true);
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return JSONBoolean.getInstance(false);
        }
        if ("null".equalsIgnoreCase(trimmed)) {
            return JSONNull.getInstance();
        }
        if (isNumber(trimmed)) {
            return new JSONNumber(Double.parseDouble(trimmed));
        }
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return JSONParser.parseStrict(trimmed);
            } catch (Exception ignore) {
                return new JSONString(value);
            }
        }
        return new JSONString(value);
    }

    private static JSONValue jsonString(String value) {
        return new JSONString(value == null ? "" : value);
    }

    private static JSONValue jsonNumber(String value) {
        if (!notEmpty(value)) {
            return new JSONNumber(0);
        }
        try {
            return new JSONNumber(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return new JSONNumber(0);
        }
    }

    private static boolean isNumber(String value) {
        return value.matches("-?\\d+(\\.\\d+)?");
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String selectedText(ListBox listBox) {
        if (listBox == null || listBox.getItemCount() == 0) {
            return "";
        }
        int index = listBox.getSelectedIndex();
        if (index < 0 || index >= listBox.getItemCount()) {
            return "";
        }
        return listBox.getItemText(index);
    }
}
