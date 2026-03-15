package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.model.ParsedData;
import com.visit.gwtapiflowbuilder.client.ui.KeyValueRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages bidirectional JSON sync between the form and the preview TextArea.
 * updateJson() → form → JSON text
 * applyJsonText() → JSON text → form (re-render)
 */
public final class FlowSyncService {

    private final AppState state;

    public FlowSyncService(AppState state) {
        this.state = state;
    }

    // -------------------------------------------------------------------------
    // Form → JSON
    // -------------------------------------------------------------------------

    public void updateJson() {
        if (state.jsonArea == null) {
            return;
        }
        if (state.suppressJsonUpdates) {
            return;
        }
        if (state.environments.isEmpty()) {
            state.suppressJsonParse = true;
            state.jsonArea.setText("{}");
            state.suppressJsonParse = false;
            if (state.previewPanel != null) state.previewPanel.setJsonStatus(true);
            return;
        }
        if (state.activeEnvIndex < 0 || state.activeEnvIndex >= state.environments.size()) {
            state.activeEnvIndex = 0;
        }
        // Sync active environment data from UI before building JSON
        syncActiveEnvironmentFromUi();
        state.suppressJsonParse = true;
        state.jsonArea.setText(
                FlowJsonBuilder.prettyPrint(
                        FlowJsonBuilder.build(
                                state.metaIdBox.getValue(),
                                state.metaVersionBox.getValue(),
                                state.environments,
                                state.activeEnvIndex,
                                state.globalInputRows,
                                state.steps)));
        state.suppressJsonParse = false;
        if (state.previewPanel != null) state.previewPanel.setJsonStatus(true);
        markDirty(state.jsonArea.getText());
    }

    // -------------------------------------------------------------------------
    // JSON → form
    // -------------------------------------------------------------------------

    public void applyJsonText(String text, boolean fromUser) {
        if (state.suppressJsonParse) {
            return;
        }
        ParsedData data = FlowParser.parseJson(text, state.environments);
        if (data == null) {
            if (fromUser) {
                if (state.previewPanel != null) state.previewPanel.setJsonStatus(false);
            } else {
                if (state.onFullReset != null) state.onFullReset.run();
            }
            return;
        }
        if (fromUser) {
            markDirty(text);
        }
        applyParsedData(data);
        if (state.onRender != null) state.onRender.run();
    }

    public void applyParsedData(ParsedData data) {
        state.suppressJsonUpdates = true;
        state.environments.clear();
        state.environments.addAll(data.environments);
        state.activeEnvIndex = data.activeEnvIndex;
        state.globalInputsData.clear();
        state.globalInputsData.addAll(data.globalInputs);
        state.stepsData.clear();
        state.stepsData.addAll(data.steps);
        state.steps.clear();
        state.envVarRows.clear();
        state.globalInputRows.clear();
        state.runtimeVariables.clear();
        state.stepCounter = 0;
        state.metaIdValue = data.metaId;
        state.metaVersionValue = data.metaVersion;
        state.suppressJsonUpdates = false;
    }

    // -------------------------------------------------------------------------
    // Dirty tracking
    // -------------------------------------------------------------------------

    public void markDirty(String json) {
        if (json == null) {
            state.dirty = false;
            return;
        }
        String currentName = state.metaIdBox == null ? AppState.DEFAULT_FLOW_NAME : state.metaIdBox.getValue();
        if (currentName == null || currentName.trim().isEmpty()) {
            currentName = AppState.DEFAULT_FLOW_NAME;
        }
        state.dirty = !json.equals(state.lastSavedJson) || !currentName.equals(state.lastSavedName);
    }

    // -------------------------------------------------------------------------
    // Bind handlers
    // -------------------------------------------------------------------------

    public void bindJsonSync() {
        state.jsonArea.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                applyJsonText(state.jsonArea.getText(), true);
            }
        });
        state.jsonArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                scheduleJsonSync();
            }
        });
    }

    public void bindWindowCloseWarning() {
        Window.addWindowClosingHandler(event -> {
            if (state.dirty) {
                event.setMessage("You have unsaved changes. Do you want to leave without saving?");
            }
        });
    }

    public void scheduleJsonSync() {
        if (state.suppressJsonParse) {
            return;
        }
        if (state.jsonSyncTimer == null) {
            state.jsonSyncTimer = new Timer() {
                @Override
                public void run() {
                    applyJsonText(state.jsonArea.getText(), true);
                }
            };
        }
        state.jsonSyncTimer.cancel();
        state.jsonSyncTimer.schedule(400);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void syncActiveEnvironmentFromUi() {
        if (state.environments.isEmpty()) return;
        EnvironmentItem env = state.environments.get(state.activeEnvIndex);
        env.timeoutMs = state.timeoutBox.getValue();
        env.retryCount = state.retryCountBox.getValue();
        env.retryDelay = state.retryDelayBox.getValue();
        env.variables = toPairs(state.envVarRows);
        for (KeyValuePair pair : env.variables) {
            if ("BASE_URL".equalsIgnoreCase(pair.key)) {
                env.baseUrl = pair.value;
                break;
            }
        }
    }

    private List<KeyValuePair> toPairs(List<KeyValueRow> rows) {
        List<KeyValuePair> pairs = new ArrayList<>();
        for (KeyValueRow row : rows) {
            pairs.add(new KeyValuePair(row.key.getValue(), row.value.getValue()));
        }
        return pairs;
    }

}
