package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.json.client.*;
import com.visit.gwtapiflowbuilder.client.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless utility class that parses JSON text into ParsedData.
 * All methods are package-private or public static — no instance state.
 */
public final class FlowParser {

    private FlowParser() {
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static ParsedData parseJson(String text, List<EnvironmentItem> fallbackEnvironments) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        JSONValue rootValue;
        try {
            rootValue = JSONParser.parseStrict(text);
        } catch (Exception parseException) {
            return null;
        }
        JSONObject root = rootValue.isObject();
        if (root == null) {
            return null;
        }

        ParsedData data = new ParsedData();
        JSONObject metadata = object(root.get("metadata"));
        data.metaId = stringValue(metadata == null ? null : metadata.get("identifier"), "default.flow.meta");
        data.metaVersion = stringValue(metadata == null ? null : metadata.get("version"), "1.0");

        JSONObject environmentsObject = object(root.get("environments"));
        String activeEnvId = stringValue(environmentsObject == null ? null : environmentsObject.get("activeEnvironmentId"), "");
        List<EnvironmentItem> parsedEnvironments = parseEnvironmentItems(environmentsObject);
        if (parsedEnvironments.isEmpty()) {
            parsedEnvironments = cloneEnvironments(fallbackEnvironments);
        }
        data.environments = parsedEnvironments;
        data.activeEnvIndex = findEnvironmentIndex(parsedEnvironments, activeEnvId);

        JSONObject globalInputs = object(root.get("globalInputs"));
        data.globalInputs = parseObjectPairs(globalInputs);

        JSONArray stepsJsonArray = array(root.get("steps"));
        data.steps = parseSteps(stepsJsonArray);

        return data;
    }

    // -------------------------------------------------------------------------
    // Parse helpers
    // -------------------------------------------------------------------------

    public static List<EnvironmentItem> parseEnvironmentItems(JSONObject environmentsObject) {
        List<EnvironmentItem> items = new ArrayList<>();
        JSONArray environmentItemsArray = environmentsObject == null ? null : array(environmentsObject.get("environmentItems"));
        if (environmentItemsArray == null) {
            return items;
        }
        for (int i = 0; i < environmentItemsArray.size(); i++) {
            JSONObject environmentJson = object(environmentItemsArray.get(i));
            if (environmentJson == null) {
                continue;
            }
            String id = stringValue(environmentJson.get("environmentId"), String.valueOf(i + 1));
            String name = stringValue(environmentJson.get("environmentName"), "ENV_" + (i + 1));
            String baseUrl = stringValue(environmentJson.get("baseUrl"), "");

            JSONObject defaults = object(environmentJson.get("defaults"));
            String timeout = stringValue(defaults == null ? null : defaults.get("timeoutMilliseconds"), "");
            JSONObject retryPolicy = object(defaults == null ? null : defaults.get("retryPolicy"));
            String retryCount = stringValue(retryPolicy == null ? null : retryPolicy.get("maximumAttempts"), "");
            String retryDelay = stringValue(retryPolicy == null ? null : retryPolicy.get("delayMilliseconds"), "");

            EnvironmentItem env = new EnvironmentItem(id, name, baseUrl, timeout, retryCount, retryDelay);
            env.variables = parseObjectPairs(object(environmentJson.get("environmentVariables")));
            items.add(env);
        }
        return items;
    }

    public static List<KeyValuePair> parseObjectPairs(JSONObject jsonObject) {
        List<KeyValuePair> pairs = new ArrayList<>();
        if (jsonObject == null) {
            return pairs;
        }
        for (String key : jsonObject.keySet()) {
            pairs.add(new KeyValuePair(key, stringValue(jsonObject.get(key), "")));
        }
        return pairs;
    }

    public static List<StepData> parseSteps(JSONArray stepsJsonArray) {
        List<StepData> stepDataList = new ArrayList<>();
        if (stepsJsonArray == null) {
            return stepDataList;
        }
        for (int i = 0; i < stepsJsonArray.size(); i++) {
            JSONObject stepObject = object(stepsJsonArray.get(i));
            if (stepObject == null) {
                continue;
            }
            String stepId = stringValue(stepObject.get("stepIdentifier"), "STEP_" + (i + 1));
            JSONObject requestObject = object(stepObject.get("request"));
            String method = stringValue(requestObject == null ? null : requestObject.get("httpMethod"), "GET");
            String url = stringValue(requestObject == null ? null : requestObject.get("requestUrl"), "");
            StepData data = new StepData(stepId, method, url);
            data.timeoutMs = stringValue(requestObject == null ? null : requestObject.get("timeoutMilliseconds"), "");
            JSONObject retryPolicy = object(requestObject == null ? null : requestObject.get("retryPolicy"));
            data.retryCount = stringValue(retryPolicy == null ? null : retryPolicy.get("maximumAttempts"), "");
            data.retryDelay = stringValue(retryPolicy == null ? null : retryPolicy.get("delayMilliseconds"), "");
            data.headers = parseObjectPairs(object(requestObject == null ? null : requestObject.get("headers")));
            data.requestVariables = parseObjectPairs(object(requestObject == null ? null : requestObject.get("requestVariables")));
            data.body = parseObjectPairs(object(requestObject == null ? null : requestObject.get("body")));

            JSONObject extractionObject = object(stepObject.get("extraction"));
            data.extractBody = parseObjectPairs(object(extractionObject == null ? null : extractionObject.get("bodyJsonPaths")));
            data.extractHeaders = parseObjectPairs(object(extractionObject == null ? null : extractionObject.get("headerValues")));

            data.checks = parseChecks(array(stepObject.get("checks")));
            stepDataList.add(data);
        }
        return stepDataList;
    }

    public static List<CheckData> parseChecks(JSONArray checksJsonArray) {
        List<CheckData> checkDataList = new ArrayList<>();
        if (checksJsonArray == null) {
            return checkDataList;
        }
        for (int i = 0; i < checksJsonArray.size(); i++) {
            JSONObject checkObject = object(checksJsonArray.get(i));
            if (checkObject == null) {
                continue;
            }
            CheckData data = new CheckData(
                    stringValue(checkObject.get("source"), "status"),
                    stringValue(checkObject.get("jsonPath"), ""),
                    stringValue(checkObject.get("equals"), ""),
                    booleanValue(checkObject.get("exists"))
            );
            checkDataList.add(data);
        }
        return checkDataList;
    }

    public static List<EnvironmentItem> cloneEnvironments(List<EnvironmentItem> source) {
        List<EnvironmentItem> copy = new ArrayList<>();
        for (EnvironmentItem env : source) {
            EnvironmentItem next = new EnvironmentItem(env.id, env.name, env.baseUrl,
                    env.timeoutMs, env.retryCount, env.retryDelay);
            next.variables = new ArrayList<>(env.variables);
            copy.add(next);
        }
        return copy;
    }

    public static int findEnvironmentIndex(List<EnvironmentItem> environments, String activeEnvId) {
        if (environments.isEmpty()) {
            return 0;
        }
        if (activeEnvId == null || activeEnvId.trim().isEmpty()) {
            return 0;
        }
        for (int i = 0; i < environments.size(); i++) {
            if (activeEnvId.equals(environments.get(i).id)) {
                return i;
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // JSON cast helpers
    // -------------------------------------------------------------------------

    public static JSONObject object(JSONValue value) {
        return value == null ? null : value.isObject();
    }

    public static JSONArray array(JSONValue value) {
        return value == null ? null : value.isArray();
    }

    public static boolean booleanValue(JSONValue value) {
        JSONBoolean bool = value == null ? null : value.isBoolean();
        return bool != null && bool.booleanValue();
    }

    public static String stringValue(JSONValue value, String fallback) {
        if (value == null) {
            return fallback;
        }
        JSONString str = value.isString();
        if (str != null) {
            return str.stringValue();
        }
        JSONNumber num = value.isNumber();
        if (num != null) {
            return numberString(num.doubleValue());
        }
        JSONBoolean bool = value.isBoolean();
        if (bool != null) {
            return String.valueOf(bool.booleanValue());
        }
        JSONNull nul = value.isNull();
        if (nul != null) {
            return "";
        }
        return value.toString();
    }

    public static double numberValueOrDefault(JSONValue value, double fallback) {
        JSONNumber num = value == null ? null : value.isNumber();
        return num == null ? fallback : num.doubleValue();
    }

    public static String numberString(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.0000001) {
            return String.valueOf(rounded);
        }
        return String.valueOf(value);
    }
}
