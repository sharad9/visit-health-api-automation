package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.visit.gwtapiflowbuilder.client.AppState;

/**
 * Handles save/load/list operations against the server flow API.
 */
public final class FlowPersistenceService {

    private static final String SAVE_ENDPOINT = "/api/flows/save";
    private static final String LOAD_ENDPOINT_BASE = "/api/flows/load?name=";
    private static final String LIST_ENDPOINT = "/api/flows/list";

    private final AppState state;

    public FlowPersistenceService(AppState state) {
        this.state = state;
    }

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    public void saveFlow(String json) {
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        String resolvedName = state.metaIdBox == null ? AppState.DEFAULT_FLOW_NAME : state.metaIdBox.getValue();
        if (resolvedName == null || resolvedName.trim().isEmpty()) {
            resolvedName = AppState.DEFAULT_FLOW_NAME;
        }
        final String flowName = resolvedName;

        Button saveBtn = state.navbarView != null ? state.navbarView.saveButton : null;
        if (saveBtn != null) {
            saveBtn.setText("Saving…");
            saveBtn.setEnabled(false);
        }

        JSONObject payload = new JSONObject();
        payload.put("name", new JSONString(flowName));
        payload.put("json", new JSONString(json));

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, SAVE_ENDPOINT);
        builder.setHeader("Content-Type", "application/json");
        try {
            builder.sendRequest(payload.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        state.lastSavedJson = json;
                        state.lastSavedName = flowName;
                        state.dirty = false;
                        loadFlowList();
                        selectFlowName(flowName);
                    }
                    if (saveBtn != null) {
                        saveBtn.setText("✓ Saved");
                        saveBtn.setEnabled(true);
                        new Timer() {
                            @Override
                            public void run() {
                                saveBtn.setText("Save");
                            }
                        }.schedule(2000);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    if (saveBtn != null) {
                        saveBtn.setText("Save");
                        saveBtn.setEnabled(true);
                    }
                }
            });
        } catch (Exception ignore) {
            if (saveBtn != null) {
                saveBtn.setText("Save");
                saveBtn.setEnabled(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    public void loadFlow() {
        loadFlowByName(AppState.DEFAULT_FLOW_NAME);
    }

    public void loadFlowByName(String name) {
        String flowName = (name == null || name.trim().isEmpty()) ? AppState.DEFAULT_FLOW_NAME : name;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, LOAD_ENDPOINT_BASE + flowName);
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() != 200) {
                        return;
                    }
                    String json = response.getText();
                    if (json == null || json.trim().isEmpty()) {
                        return;
                    }
                    state.lastSavedJson = json;
                    state.lastSavedName = flowName;
                    state.dirty = false;
                    if (state.onLoadComplete != null) {
                        state.onLoadComplete.accept(json);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    // no-op
                }
            });
        } catch (Exception ignore) {
        }
    }

    // -------------------------------------------------------------------------
    // List
    // -------------------------------------------------------------------------

    public void loadFlowList() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, LIST_ENDPOINT);
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() != 200) {
                        return;
                    }
                    JSONValue value;
                    try {
                        value = JSONParser.parseStrict(response.getText());
                    } catch (Exception ex) {
                        return;
                    }
                    JSONArray arr = value.isArray();
                    if (arr == null || state.flowSelect == null) {
                        return;
                    }
                    state.flowSelect.clear();
                    state.flowSelect.addItem("Load flow...");
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject obj = arr.get(i).isObject();
                        if (obj == null) continue;
                        String name = FlowParser.stringValue(obj.get("name"), "");
                        if (!name.isEmpty()) {
                            state.flowSelect.addItem(name);
                        }
                    }
                    selectFlowName(state.lastSavedName);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    // no-op
                }
            });
        } catch (Exception ignore) {
        }
    }

    public void selectFlowName(String name) {
        if (state.flowSelect == null || name == null) {
            return;
        }
        for (int i = 0; i < state.flowSelect.getItemCount(); i++) {
            if (name.equals(state.flowSelect.getItemText(i))) {
                state.flowSelect.setSelectedIndex(i);
                return;
            }
        }
    }
}
