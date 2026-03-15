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

        EnvironmentItem activeEnvironment = environments.get(activeEnvIndex);
        JSONObject environmentObject = new JSONObject();
        if (notEmpty(activeEnvironment.baseUrl)) {
            environmentObject.put("baseUrl", jsonString(activeEnvironment.baseUrl));
        }
        environmentObject.put("defaults", defaultsObject(activeEnvironment.timeoutMs, activeEnvironment.retryCount, activeEnvironment.retryDelay));
        root.put("environment", environmentObject);

        JSONObject environmentsJson = new JSONObject();
        environmentsJson.put("activeEnvironmentId", jsonString(activeEnvironment.id));
        JSONArray environmentItemsArray = new JSONArray();
        for (int i = 0; i < environments.size(); i++) {
            EnvironmentItem env = environments.get(i);
            JSONObject envJson = new JSONObject();
            envJson.put("environmentId", jsonString(env.id));
            envJson.put("environmentName", jsonString(env.name));
            if (notEmpty(env.baseUrl)) {
                envJson.put("baseUrl", jsonString(env.baseUrl));
            }
            envJson.put("defaults", defaultsObject(env.timeoutMs, env.retryCount, env.retryDelay));
            JSONObject environmentVariablesObject = toObject(env.variables);
            if (environmentVariablesObject.size() > 0) {
                envJson.put("environmentVariables", environmentVariablesObject);
            }
            environmentItemsArray.set(i, envJson);
        }
        environmentsJson.put("environmentItems", environmentItemsArray);
        root.put("environments", environmentsJson);

        JSONObject globalInputs = toObject(toPairs(globalInputRows));
        if (globalInputs.size() > 0) {
            root.put("globalInputs", globalInputs);
        }

        JSONArray stepsJsonArray = new JSONArray();
        for (int i = 0; i < steps.size(); i++) {
            StepBlock step = steps.get(i);
            JSONObject stepJson = new JSONObject();
            stepJson.put("stepIdentifier", jsonString(step.stepId.getValue()));

            JSONObject requestObject = new JSONObject();
            requestObject.put("httpMethod", jsonString(selectedText(step.method)));
            requestObject.put("requestUrl", jsonString(step.url.getValue()));
            if (notEmpty(step.timeoutMs.getValue())) {
                requestObject.put("timeoutMilliseconds", jsonNumber(step.timeoutMs.getValue()));
            }
            if (notEmpty(step.retryCount.getValue())) {
                JSONObject retryPolicyObject = new JSONObject();
                retryPolicyObject.put("maximumAttempts", jsonNumber(step.retryCount.getValue()));
                retryPolicyObject.put("delayMilliseconds", jsonNumber(step.retryDelay.getValue()));
                requestObject.put("retryPolicy", retryPolicyObject);
            }

            JSONObject headersObject = toObject(toPairs(step.headers));
            if (headersObject.size() > 0) {
                requestObject.put("headers", headersObject);
            }
            JSONObject requestVariablesObject = toObject(toPairs(step.requestVariables));
            if (requestVariablesObject.size() > 0) {
                requestObject.put("requestVariables", requestVariablesObject);
            }
            JSONObject bodyObject = toObjectParsed(step.body);
            if (bodyObject.size() > 0) {
                requestObject.put("body", bodyObject);
            }
            stepJson.put("request", requestObject);

            JSONObject extractionObject = new JSONObject();
            JSONObject extractBodyObject = toObject(toPairs(step.extractBody));
            JSONObject extractHeadersObject = toObject(toPairs(step.extractHeaders));
            if (extractBodyObject.size() > 0) {
                extractionObject.put("bodyJsonPaths", extractBodyObject);
            }
            if (extractHeadersObject.size() > 0) {
                extractionObject.put("headerValues", extractHeadersObject);
            }
            if (extractionObject.size() > 0) {
                stepJson.put("extraction", extractionObject);
            }

            JSONArray checksArray = toChecks(step.checks);
            if (checksArray.size() > 0) {
                stepJson.put("checks", checksArray);
            }

            stepsJsonArray.set(i, stepJson);
        }
        root.put("steps", stepsJsonArray);

        return root;
    }

    private static JSONObject defaultsObject(String timeout, String count, String delay) {
        JSONObject defaults = new JSONObject();
        defaults.put("timeoutMilliseconds", jsonNumber(timeout));
        JSONObject retryPolicyObject = new JSONObject();
        retryPolicyObject.put("maximumAttempts", jsonNumber(count));
        retryPolicyObject.put("delayMilliseconds", jsonNumber(delay));
        defaults.put("retryPolicy", retryPolicyObject);
        return defaults;
    }

    private static JSONObject toObject(List<KeyValuePair> pairs) {
        JSONObject result = new JSONObject();
        for (KeyValuePair pair : pairs) {
            if (notEmpty(pair.key)) {
                result.put(pair.key, jsonString(pair.value));
            }
        }
        return result;
    }

    private static JSONObject toObjectParsed(List<KeyValueRow> rows) {
        JSONObject result = new JSONObject();
        for (KeyValueRow row : rows) {
            String key = row.key.getValue();
            if (notEmpty(key)) {
                result.put(key, parseValue(row.value.getValue()));
            }
        }
        return result;
    }

    private static JSONArray toChecks(List<CheckRow> rows) {
        JSONArray result = new JSONArray();
        int insertIndex = 0;
        for (CheckRow row : rows) {
            String source = selectedText(row.source);
            if (!notEmpty(source)) {
                continue;
            }
            JSONObject checkObject = new JSONObject();
            checkObject.put("source", jsonString(source));
            if (notEmpty(row.path.getValue())) {
                checkObject.put("jsonPath", jsonString(row.path.getValue()));
            }
            if (notEmpty(row.equals.getValue())) {
                checkObject.put("equals", parseValue(row.equals.getValue()));
            }
            if (row.exists.getValue()) {
                checkObject.put("exists", JSONBoolean.getInstance(true));
            }
            result.set(insertIndex++, checkObject);
        }
        return result;
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
        } catch (NumberFormatException numberFormatException) {
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
        int selectedIndex = listBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= listBox.getItemCount()) {
            return "";
        }
        return listBox.getItemText(selectedIndex);
    }
}
