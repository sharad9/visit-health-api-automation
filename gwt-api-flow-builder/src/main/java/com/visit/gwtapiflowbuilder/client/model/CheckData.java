package com.visit.gwtapiflowbuilder.client.model;

public final class CheckData {
    public final String source;
    public final String jsonPath;
    public final String equalsValue;
    public final boolean exists;

    public CheckData(String source, String jsonPath, String equalsValue, boolean exists) {
        this.source = source == null ? "" : source;
        this.jsonPath = jsonPath == null ? "" : jsonPath;
        this.equalsValue = equalsValue == null ? "" : equalsValue;
        this.exists = exists;
    }
}
