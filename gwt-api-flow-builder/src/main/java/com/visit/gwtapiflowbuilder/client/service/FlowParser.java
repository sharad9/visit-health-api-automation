package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.visit.gwtapiflowbuilder.client.model.CheckData;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.model.ParsedData;
import com.visit.gwtapiflowbuilder.client.model.StepData;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless utility class that parses JSON text into ParsedData.
 * All methods are package-private or public static — no instance state.
 */
public final class FlowParser {

    private FlowParser() {}

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
        } catch (Exception e) {
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

        JSONObject envsObj = object(root.get("environments"));
        String activeEnvId = stringValue(envsObj == null ? null : envsObj.get("activeEnvironmentId"), "");
        List<EnvironmentItem> parsedEnvs = parseEnvironmentItems(envsObj);
        if (parsedEnvs.isEmpty()) {
            parsedEnvs = cloneEnvironments(fallbackEnvironments);
        }
        data.environments = parsedEnvs;
        data.activeEnvIndex = findEnvironmentIndex(parsedEnvs, activeEnvId);

        JSONObject globalInputs = object(root.get("globalInputs"));
        data.globalInputs = parseObjectPairs(globalInputs);

        JSONArray stepsArr = array(root.get("steps"));
        data.steps = parseSteps(stepsArr);

        return data;
    }

    // -------------------------------------------------------------------------
    // Parse helpers
    // -------------------------------------------------------------------------

    public static List<EnvironmentItem> parseEnvironmentItems(JSONObject envsObj) {
        List<EnvironmentItem> items = new ArrayList<>();
        JSONArray envItems = envsObj == null ? null : array(envsObj.get("environmentItems"));
        if (envItems == null) {
            return items;
        }
        for (int i = 0; i < envItems.size(); i++) {
            JSONObject envJson = object(envItems.get(i));
            if (envJson == null) {
                continue;
            }
            String id = stringValue(envJson.get("environmentId"), String.valueOf(i + 1));
            String name = stringValue(envJson.get("environmentName"), "ENV_" + (i + 1));
            String baseUrl = stringValue(envJson.get("baseUrl"), "");

            JSONObject defaults = object(envJson.get("defaults"));
            String timeout = stringValue(defaults == null ? null : defaults.get("timeoutMilliseconds"), "");
            JSONObject retry = object(defaults == null ? null : defaults.get("retryPolicy"));
            String retryCount = stringValue(retry == null ? null : retry.get("maximumAttempts"), "");
            String retryDelay = stringValue(retry == null ? null : retry.get("delayMilliseconds"), "");

            EnvironmentItem env = new EnvironmentItem(id, name, baseUrl, timeout, retryCount, retryDelay);
            env.variables = parseObjectPairs(object(envJson.get("environmentVariables")));
            items.add(env);
        }
        return items;
    }

    public static List<KeyValuePair> parseObjectPairs(JSONObject obj) {
        List<KeyValuePair> pairs = new ArrayList<>();
        if (obj == null) {
            return pairs;
        }
        for (String key : obj.keySet()) {
            pairs.add(new KeyValuePair(key, stringValue(obj.get(key), "")));
        }
        return pairs;
    }

    public static List<StepData> parseSteps(JSONArray stepsArr) {
        List<StepData> list = new ArrayList<>();
        if (stepsArr == null) {
            return list;
        }
        for (int i = 0; i < stepsArr.size(); i++) {
            JSONObject stepObj = object(stepsArr.get(i));
            if (stepObj == null) {
                continue;
            }
            String stepId = stringValue(stepObj.get("stepIdentifier"), "STEP_" + (i + 1));
            JSONObject request = object(stepObj.get("request"));
            String method = stringValue(request == null ? null : request.get("httpMethod"), "GET");
            String url = stringValue(request == null ? null : request.get("requestUrl"), "");
            StepData data = new StepData(stepId, method, url);
            data.timeoutMs = stringValue(request == null ? null : request.get("timeoutMilliseconds"), "");
            JSONObject retry = object(request == null ? null : request.get("retryPolicy"));
            data.retryCount = stringValue(retry == null ? null : retry.get("maximumAttempts"), "");
            data.retryDelay = stringValue(retry == null ? null : retry.get("delayMilliseconds"), "");
            data.headers = parseObjectPairs(object(request == null ? null : request.get("headers")));
            data.requestVariables = parseObjectPairs(object(request == null ? null : request.get("requestVariables")));
            data.body = parseObjectPairs(object(request == null ? null : request.get("body")));

            JSONObject extraction = object(stepObj.get("extraction"));
            data.extractBody = parseObjectPairs(object(extraction == null ? null : extraction.get("bodyJsonPaths")));
            data.extractHeaders = parseObjectPairs(object(extraction == null ? null : extraction.get("headerValues")));

            data.checks = parseChecks(array(stepObj.get("checks")));
            list.add(data);
        }
        return list;
    }

    public static List<CheckData> parseChecks(JSONArray checksArr) {
        List<CheckData> list = new ArrayList<>();
        if (checksArr == null) {
            return list;
        }
        for (int i = 0; i < checksArr.size(); i++) {
            JSONObject checkObj = object(checksArr.get(i));
            if (checkObj == null) {
                continue;
            }
            CheckData data = new CheckData(
                    stringValue(checkObj.get("source"), "status"),
                    stringValue(checkObj.get("jsonPath"), ""),
                    stringValue(checkObj.get("equals"), ""),
                    booleanValue(checkObj.get("exists"))
            );
            list.add(data);
        }
        return list;
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

    public static int findEnvironmentIndex(List<EnvironmentItem> envs, String activeEnvId) {
        if (envs.isEmpty()) {
            return 0;
        }
        if (activeEnvId == null || activeEnvId.trim().isEmpty()) {
            return 0;
        }
        for (int i = 0; i < envs.size(); i++) {
            if (activeEnvId.equals(envs.get(i).id)) {
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
