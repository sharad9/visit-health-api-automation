package com.visit.gwtapiflowbuilder.client.model;

import java.util.ArrayList;
import java.util.List;

public final class ParsedData {
    public String metaId;
    public String metaVersion;
    public List<EnvironmentItem> environments = new ArrayList<>();
    public int activeEnvIndex;
    public List<KeyValuePair> globalInputs = new ArrayList<>();
    public List<StepData> steps = new ArrayList<>();
}
