package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

/**
 * Builds the Environment section UI and manages environment CRUD operations.
 */
public final class EnvironmentSection {

    private final AppState state;
    private final FormBuilder formBuilder;
    private final TokenRenderer tokenRenderer;

    public EnvironmentSection(AppState state, FormBuilder formBuilder, TokenRenderer tokenRenderer) {
        this.state = state;
        this.formBuilder = formBuilder;
        this.tokenRenderer = tokenRenderer;
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    public FlowPanel build() {
        FlowPanel body = new FlowPanel();
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        body.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        Button addEnv = UiFactory.outlineButton("＋ Add Environment");
        addEnv.addClickHandler(event -> addEnvironment());
        Button removeEnv = UiFactory.removeButton("✖");
        removeEnv.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "28px");
        removeEnv.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "24px");
        removeEnv.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "0");
        removeEnv.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_SELF, BaseStyle.Value.CENTER);
        removeEnv.addClickHandler(event -> removeEnvironment());

        FlowPanel row1 = formBuilder.row("auto 1fr auto");
        state.envSelect = formBuilder.selectEnvironments();
        row1.add(addEnv);
        row1.add(UiFactory.field("Active Environment", state.envSelect));

        FlowPanel rowName = new FlowPanel();
        rowName.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        rowName.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        FlowPanel nameLabelRow = new FlowPanel();
        nameLabelRow.add(UiFactory.smallLabel("Environment Name"));
        state.envNameBox = formBuilder.textBox("", "ENV_NAME");
        state.envNameBox.setReadOnly(true);
        state.envNamePreview = tokenRenderer.tokenPreview(state.envNameBox);
        FlowPanel nameFieldRow = formBuilder.row("1fr auto");
        nameFieldRow.add(tokenRenderer.previewToggle(state.envNameBox, state.envNamePreview,
                () -> commitEnvironmentName()));
        nameFieldRow.add(removeEnv);
        rowName.add(nameLabelRow);
        rowName.add(nameFieldRow);

        FlowPanel row2 = formBuilder.row();
        state.timeoutBox = formBuilder.numberBox("10000");
        state.retryCountBox = formBuilder.numberBox("2");
        state.retryDelayBox = formBuilder.numberBox("500");
        row2.add(UiFactory.field("Timeout (ms)", tokenRenderer.previewToggle(state.timeoutBox)));
        row2.add(UiFactory.field("Retry Count", tokenRenderer.previewToggle(state.retryCountBox)));
        row2.add(UiFactory.field("Retry Delay", tokenRenderer.previewToggle(state.retryDelayBox)));

        FlowPanel varsSection = new FlowPanel();
        varsSection.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        varsSection.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);
        Label varsLabel = UiFactory.smallLabel("Environment Variables");
        varsSection.add(varsLabel);

        state.envVarsList = new FlowPanel();
        state.envVarsList.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        state.envVarsList.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        varsSection.add(state.envVarsList);

        Button addVar = UiFactory.ghostButton("＋ Add Variable");
        addVar.addClickHandler(event -> {
            formBuilder.addKeyValueRow(state.envVarsList, state.envVarRows, "", "", "KEY", "VALUE");
            if (state.onUpdate != null) state.onUpdate.run();
        });
        varsSection.add(addVar);

        body.add(row1);
        body.add(rowName);
        body.add(row2);
        body.add(varsSection);

        state.envSelect.addChangeHandler((ChangeHandler) event -> {
            syncActiveEnvironmentFromUi();
            state.activeEnvIndex = state.envSelect.getSelectedIndex();
            loadEnvironmentIntoUi(state.environments.get(state.activeEnvIndex));
            if (state.onUpdate != null) state.onUpdate.run();
        });
        formBuilder.bindUpdate(state.timeoutBox);
        formBuilder.bindUpdate(state.retryCountBox);
        formBuilder.bindUpdate(state.retryDelayBox);

        return body;
    }

    // -------------------------------------------------------------------------
    // Environment name commit
    // -------------------------------------------------------------------------

    public void commitEnvironmentName() {
        if (state.envNameBox == null || state.environments.isEmpty()) {
            return;
        }
        String name = state.envNameBox.getValue();
        if (name == null) name = "";
        name = name.trim();
        if (name.isEmpty()) {
            String existing = state.environments.get(state.activeEnvIndex).name;
            state.envNameBox.setValue(existing);
            state.envNameBox.setReadOnly(true);
            if (state.envNamePreview != null) tokenRenderer.renderTokenPreview(state.envNamePreview, existing);
            return;
        }
        state.environments.get(state.activeEnvIndex).name = name;
        state.envNameBox.setReadOnly(true);
        if (state.envNamePreview != null) tokenRenderer.renderTokenPreview(state.envNamePreview, name);
        refreshEnvironmentSelect();
        if (state.onUpdate != null) state.onUpdate.run();
    }

    // -------------------------------------------------------------------------
    // Add / remove environment
    // -------------------------------------------------------------------------

    public void addEnvironment() {
        String name = "ENV_" + (state.envCounter + 1);
        String id = String.valueOf(++state.envCounter);
        EnvironmentItem env = new EnvironmentItem(id, name, "", "10000", "2", "500");
        state.environments.add(env);
        state.activeEnvIndex = state.environments.size() - 1;
        refreshEnvironmentSelect();
        loadEnvironmentIntoUi(env);
        if (state.envNameBox != null) {
            state.envNameBox.setReadOnly(false);
            state.envNameBox.setFocus(true);
            state.envNameBox.selectAll();
        }
        if (state.onUpdate != null) state.onUpdate.run();
    }

    public void removeEnvironment() {
        if (state.environments.size() <= 1) {
            Window.alert("At least one environment is required.");
            return;
        }
        state.environments.remove(state.activeEnvIndex);
        if (state.activeEnvIndex >= state.environments.size()) {
            state.activeEnvIndex = state.environments.size() - 1;
        }
        refreshEnvironmentSelect();
        loadEnvironmentIntoUi(state.environments.get(state.activeEnvIndex));
        if (state.onUpdate != null) state.onUpdate.run();
    }

    // -------------------------------------------------------------------------
    // Load environment into UI fields
    // -------------------------------------------------------------------------

    public void loadEnvironmentIntoUi(EnvironmentItem env) {
        if (state.envNameBox != null) {
            state.envNameBox.setValue(env.name);
            state.envNameBox.setReadOnly(true);
            if (state.envNamePreview != null) {
                tokenRenderer.renderTokenPreview(state.envNamePreview, env.name);
            }
        }
        state.timeoutBox.setValue(env.timeoutMs);
        state.retryCountBox.setValue(env.retryCount);
        state.retryDelayBox.setValue(env.retryDelay);

        state.envVarRows.clear();
        state.envVarsList.clear();
        if (env.variables.isEmpty()) {
            formBuilder.addKeyValueRow(state.envVarsList, state.envVarRows,
                "BASE_URL", env.baseUrl == null ? "" : env.baseUrl, "KEY", "VALUE");
            formBuilder.addKeyValueRow(state.envVarsList, state.envVarRows, "", "", "KEY", "VALUE");
        } else {
            boolean hasBaseUrl = false;
            for (KeyValuePair pair : env.variables) {
                if ("BASE_URL".equalsIgnoreCase(pair.key)) {
                    hasBaseUrl = true;
                }
                formBuilder.addKeyValueRow(state.envVarsList, state.envVarRows,
                    pair.key, pair.value, "KEY", "VALUE");
            }
            if (!hasBaseUrl) {
                formBuilder.addKeyValueRow(state.envVarsList, state.envVarRows,
                    "BASE_URL", env.baseUrl == null ? "" : env.baseUrl, "KEY", "VALUE");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Sync active environment data back from UI before JSON build
    // -------------------------------------------------------------------------

    public void syncActiveEnvironmentFromUi() {
        if (state.environments.isEmpty()) return;
        EnvironmentItem env = state.environments.get(state.activeEnvIndex);
        env.timeoutMs = state.timeoutBox.getValue();
        env.retryCount = state.retryCountBox.getValue();
        env.retryDelay = state.retryDelayBox.getValue();
        env.variables = formBuilder.toPairs(state.envVarRows);
        for (KeyValuePair pair : env.variables) {
            if ("BASE_URL".equalsIgnoreCase(pair.key)) {
                env.baseUrl = pair.value;
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Refresh select dropdown
    // -------------------------------------------------------------------------

    public void refreshEnvironmentSelect() {
        if (state.envSelect == null) return;
        state.envSelect.clear();
        for (EnvironmentItem env : state.environments) {
            state.envSelect.addItem(env.name);
        }
        if (state.activeEnvIndex >= 0 && state.activeEnvIndex < state.envSelect.getItemCount()) {
            state.envSelect.setSelectedIndex(state.activeEnvIndex);
        }
    }
}
