package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

/**
 * Triggers server-side collection run and displays the run report overlay.
 */
public final class CollectionRunnerView {

    private final AppState state;

    public CollectionRunnerView(AppState state) {
        this.state = state;
    }

    // -------------------------------------------------------------------------
    // Run collection
    // -------------------------------------------------------------------------

    public static native void openReportInNewTab(String url) /*-{
        $wnd.open(url, '_blank');
    }-*/;

    // -------------------------------------------------------------------------
    // Run report overlay
    // -------------------------------------------------------------------------

    public void runCollection() {
        String flowName = state.metaIdBox == null ? null : state.metaIdBox.getValue();
        if (flowName == null || flowName.trim().isEmpty()) {
            Window.alert("Save the flow first before running.");
            return;
        }
        if (state.navbarView != null && state.navbarView.runButton != null) {
            state.navbarView.runButton.setText("Running…");
            state.navbarView.runButton.setEnabled(false);
        }
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, "/api/flows/run?name=" + flowName);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    resetRunButton();
                    if (response.getStatusCode() == 200) {
                        showRunReport(response.getText());
                    } else {
                        Window.alert("Run failed: " + response.getStatusCode() + "\n" + response.getText());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    resetRunButton();
                    Window.alert("Run error: " + exception.getMessage());
                }
            });
        } catch (RequestException requestException) {
            resetRunButton();
            Window.alert("Run error: " + requestException.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Stat badge
    // -------------------------------------------------------------------------

    public void showRunReport(String json) {
        JSONObject reportObject;
        try {
            reportObject = JSONParser.parseStrict(json).isObject();
        } catch (Exception exception) {
            Window.alert("Could not parse report: " + exception.getMessage());
            return;
        }
        if (reportObject == null) {
            Window.alert("Unexpected report response.");
            return;
        }

        int total = (int) reportObject.get("totalSteps").isNumber().doubleValue();
        int passed = (int) reportObject.get("passedSteps").isNumber().doubleValue();
        int failed = (int) reportObject.get("failedSteps").isNumber().doubleValue();
        long totalDurationMs = (long) reportObject.get("totalDurationMs").isNumber().doubleValue();
        String reportUrl = reportObject.get("reportUrl").isString().stringValue();
        String executedAt = reportObject.get("executedAt").isString().stringValue();

        // Backdrop
        FlowPanel overlay = new FlowPanel();
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.POSITION, BaseStyle.Value.FIXED);
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.INSET, "0");
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, "rgba(15,23,42,0.55)");
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.Z_INDEX, "100");
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        overlay.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.CENTER);

        // Card
        FlowPanel card = new FlowPanel();
        card.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        card.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        card.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "14px");
        card.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "26px 30px");
        card.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "400px");
        card.getElement().getStyle().setProperty(BaseStyle.Key.MAX_WIDTH, "540px");
        card.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "90vw");
        card.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 20px 60px rgba(0,0,0,0.35)");
        card.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        card.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "14px");

        // Title row
        FlowPanel titleRow = new FlowPanel();
        titleRow.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        titleRow.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        titleRow.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        Label title = new Label("▶ Run Report");
        title.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, "700");
        title.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "15px");
        title.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        Button closeButton = UiFactory.ghostButton("✕");
        closeButton.addClickHandler(event -> state.root.remove(overlay));
        titleRow.add(title);
        titleRow.add(closeButton);
        card.add(titleRow);

        // Executed at
        Label executedAtLabel = new Label("Executed: " + executedAt);
        executedAtLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        executedAtLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
        card.add(executedAtLabel);

        // Stats row
        FlowPanel statsRow = new FlowPanel();
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "repeat(4,1fr)");
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "8px");
        statsRow.add(statBadge("Total", String.valueOf(total), "#2563eb"));
        statsRow.add(statBadge("Passed", String.valueOf(passed), "#16a34a"));
        statsRow.add(statBadge("Failed", String.valueOf(failed), failed > 0 ? "#dc2626" : "#16a34a"));
        statsRow.add(statBadge("Duration", totalDurationMs + " ms", "#7c3aed"));
        card.add(statsRow);

        // Step list
        JSONValue stepsJsonValue = reportObject.get("steps");
        JSONArray stepResultsArray = stepsJsonValue == null ? null : stepsJsonValue.isArray();
        if (stepResultsArray != null) {
            FlowPanel stepList = new FlowPanel();
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "4px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.MAX_HEIGHT, "220px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW_Y, BaseStyle.Value.AUTO);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "4px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_BG);

            for (int index = 0; index < stepResultsArray.size(); index++) {
                JSONObject stepResult = stepResultsArray.get(index).isObject();
                if (stepResult == null) continue;
                boolean stepPassed = stepResult.get("passed").isBoolean().booleanValue();
                String stepIdentifier = stepResult.get("stepIdentifier").isString().stringValue();
                long stepDurationMs = (long) stepResult.get("durationMs").isNumber().doubleValue();
                int stepStatusCode = (int) stepResult.get("statusCode").isNumber().doubleValue();

                FlowPanel row = new FlowPanel();
                row.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
                row.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
                row.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "8px");
                row.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "5px 8px");
                row.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "6px");
                row.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
                row.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_LEFT,
                        "3px solid " + (stepPassed ? "#16a34a" : "#dc2626"));

                Label icon = new Label(stepPassed ? "✓" : "✗");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, stepPassed ? "#16a34a" : "#dc2626");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, "700");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "16px");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "12px");

                Label name = new Label(stepIdentifier);
                name.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "1");
                name.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "12px");
                name.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);

                Label meta = new Label(stepStatusCode + "  " + stepDurationMs + " ms");
                meta.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "11px");
                meta.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
                meta.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_MONO);

                row.add(icon);
                row.add(name);
                row.add(meta);
                stepList.add(row);
            }
            card.add(stepList);
        }

        // View report button
        Button viewReportButton = UiFactory.outlineButton("View Full Report ↗");
        viewReportButton.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        viewReportButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEMPLATE);
        viewReportButton.addClickHandler(event -> openReportInNewTab(reportUrl));
        card.add(viewReportButton);

        overlay.add(card);
        state.root.add(overlay);
    }

    public FlowPanel statBadge(String label, String value, String color) {
        FlowPanel badgePanel = new FlowPanel();
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_TOP, "3px solid " + color);
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "10px 8px");
        badgePanel.getElement().getStyle().setProperty(BaseStyle.Key.TEXT_ALIGN, BaseStyle.Value.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, "700");
        valueLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, color);
        valueLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "18px");

        Label titleLabel = new Label(label);
        titleLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "10px");
        titleLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);

        badgePanel.add(valueLabel);
        badgePanel.add(titleLabel);
        return badgePanel;
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private void resetRunButton() {
        if (state.navbarView != null && state.navbarView.runButton != null) {
            state.navbarView.runButton.setText("▶ Run Collection");
            state.navbarView.runButton.setEnabled(true);
        }
    }
}
