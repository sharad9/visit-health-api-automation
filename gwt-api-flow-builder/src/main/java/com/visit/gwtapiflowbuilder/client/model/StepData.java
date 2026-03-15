package com.visit.gwtapiflowbuilder.client.model;

import java.util.ArrayList;
import java.util.List;

public final class StepData {
    public final String stepId;
    public final String method;
    public final String url;
    public String timeoutMs = "";
    public String retryCount = "";
    public String retryDelay = "";
    public List<KeyValuePair> headers = new ArrayList<>();
    public List<KeyValuePair> requestVariables = new ArrayList<>();
    public List<KeyValuePair> body = new ArrayList<>();
    public List<KeyValuePair> extractBody = new ArrayList<>();
    public List<KeyValuePair> extractHeaders = new ArrayList<>();
    public List<CheckData> checks = new ArrayList<>();

    public StepData(String stepId, String method, String url) {
        this.stepId = stepId == null ? "" : stepId;
        this.method = method == null ? "GET" : method;
        this.url = url == null ? "" : url;
    }
}
