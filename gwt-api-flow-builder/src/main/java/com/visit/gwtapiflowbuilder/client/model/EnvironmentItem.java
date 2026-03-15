package com.visit.gwtapiflowbuilder.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnvironmentItem {
    public final String id;
    public String name;
    public String baseUrl;
    public String timeoutMs;
    public String retryCount;
    public String retryDelay;
    public List<KeyValuePair> variables = new ArrayList<>();

    public EnvironmentItem(String id, String name, String baseUrl,
                           String timeoutMs, String retryCount, String retryDelay,
                           KeyValuePair... vars) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.retryCount = retryCount;
        this.retryDelay = retryDelay;
        if (vars != null) {
            Collections.addAll(this.variables, vars);
        }
    }
}
