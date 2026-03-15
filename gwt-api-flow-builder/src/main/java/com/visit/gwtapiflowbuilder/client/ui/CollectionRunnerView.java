package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.service.FlowParser;
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
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "/api/flows/run?name=" + flowName);
        try {
            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request req, Response resp) {
                    resetRunButton();
                    if (resp.getStatusCode() == 200) {
                        showRunReport(resp.getText());
                    } else {
                        Window.alert("Run failed: " + resp.getStatusCode() + "\n" + resp.getText());
                    }
                }

                @Override
                public void onError(Request req, Throwable ex) {
                    resetRunButton();
                    Window.alert("Run error: " + ex.getMessage());
                }
            });
        } catch (RequestException ex) {
            resetRunButton();
            Window.alert("Run error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Run report overlay
    // -------------------------------------------------------------------------

    public void showRunReport(String json) {
        JSONObject obj;
        try {
            obj = JSONParser.parseStrict(json).isObject();
        } catch (Exception e) {
            Window.alert("Could not parse report: " + e.getMessage());
            return;
        }
        if (obj == null) {
            Window.alert("Unexpected report response.");
            return;
        }

        int total      = (int) obj.get("totalSteps").isNumber().doubleValue();
        int passed     = (int) obj.get("passedSteps").isNumber().doubleValue();
        int failed     = (int) obj.get("failedSteps").isNumber().doubleValue();
        long ms        = (long) obj.get("totalDurationMs").isNumber().doubleValue();
        String reportUrl = obj.get("reportUrl").isString().stringValue();
        String execAt    = obj.get("executedAt").isString().stringValue();

        FlowPanel overlay = new FlowPanel();
        overlay.getElement().getStyle().setProperty("position", "fixed");
        overlay.getElement().getStyle().setProperty("inset", "0");
        overlay.getElement().getStyle().setProperty("background", "rgba(15,23,42,0.55)");
        overlay.getElement().getStyle().setProperty("zIndex", "100");
        overlay.getElement().getStyle().setProperty("display", "flex");
        overlay.getElement().getStyle().setProperty("alignItems", "center");
        overlay.getElement().getStyle().setProperty("justifyContent", "center");

        FlowPanel card = new FlowPanel();
        card.getElement().getStyle().setProperty("background", Theme.COLOR_PANEL);
        card.getElement().getStyle().setProperty("border", "1px solid " + Theme.COLOR_BORDER);
        card.getElement().getStyle().setProperty("borderRadius", "14px");
        card.getElement().getStyle().setProperty("padding", "26px 30px");
        card.getElement().getStyle().setProperty("minWidth", "400px");
        card.getElement().getStyle().setProperty("maxWidth", "540px");
        card.getElement().getStyle().setProperty("width", "90vw");
        card.getElement().getStyle().setProperty("boxShadow", "0 20px 60px rgba(0,0,0,0.35)");
        card.getElement().getStyle().setProperty("display", "grid");
        card.getElement().getStyle().setProperty("gap", "14px");

        FlowPanel titleRow = new FlowPanel();
        titleRow.getElement().getStyle().setProperty("display", "flex");
        titleRow.getElement().getStyle().setProperty("alignItems", "center");
        titleRow.getElement().getStyle().setProperty("justifyContent", "space-between");
        Label title = new Label("▶ Run Report");
        title.getElement().getStyle().setProperty("fontWeight", "700");
        title.getElement().getStyle().setProperty("fontSize", "15px");
        title.getElement().getStyle().setProperty("color", Theme.COLOR_TEXT);
        Button closeBtn = UiFactory.ghostButton("✕");
        closeBtn.addClickHandler(e -> state.root.remove(overlay));
        titleRow.add(title);
        titleRow.add(closeBtn);
        card.add(titleRow);

        Label atLabel = new Label("Executed: " + execAt);
        atLabel.getElement().getStyle().setProperty("fontSize", Theme.FONT_SIZE_78);
        atLabel.getElement().getStyle().setProperty("color", Theme.COLOR_MUTED);
        card.add(atLabel);

        FlowPanel statsRow = new FlowPanel();
        statsRow.getElement().getStyle().setProperty("display", "grid");
        statsRow.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(4,1fr)");
        statsRow.getElement().getStyle().setProperty("gap", "8px");
        statsRow.add(statBadge("Total", String.valueOf(total), "#2563eb"));
        statsRow.add(statBadge("Passed", String.valueOf(passed), "#16a34a"));
        statsRow.add(statBadge("Failed", String.valueOf(failed), failed > 0 ? "#dc2626" : "#16a34a"));
        statsRow.add(statBadge("Duration", ms + " ms", "#7c3aed"));
        card.add(statsRow);

        JSONValue stepsVal = obj.get("steps");
        JSONArray stepsArr = stepsVal == null ? null : stepsVal.isArray();
        if (stepsArr != null) {
            FlowPanel stepList = new FlowPanel();
            stepList.getElement().getStyle().setProperty("display", "grid");
            stepList.getElement().getStyle().setProperty("gap", "4px");
            stepList.getElement().getStyle().setProperty("maxHeight", "220px");
            stepList.getElement().getStyle().setProperty("overflowY", "auto");
            stepList.getElement().getStyle().setProperty("padding", "4px");
            stepList.getElement().getStyle().setProperty("border", "1px solid " + Theme.COLOR_BORDER);
            stepList.getElement().getStyle().setProperty("borderRadius", "8px");
            stepList.getElement().getStyle().setProperty("background", Theme.COLOR_BG);
            for (int i = 0; i < stepsArr.size(); i++) {
                JSONObject s = stepsArr.get(i).isObject();
                if (s == null) continue;
                boolean stepPassed = s.get("passed").isBoolean().booleanValue();
                String sid  = s.get("stepIdentifier").isString().stringValue();
                long sdur   = (long) s.get("durationMs").isNumber().doubleValue();
                int scode   = (int) s.get("statusCode").isNumber().doubleValue();

                FlowPanel row = new FlowPanel();
                row.getElement().getStyle().setProperty("display", "flex");
                row.getElement().getStyle().setProperty("alignItems", "center");
                row.getElement().getStyle().setProperty("gap", "8px");
                row.getElement().getStyle().setProperty("padding", "5px 8px");
                row.getElement().getStyle().setProperty("borderRadius", "6px");
                row.getElement().getStyle().setProperty("background", Theme.COLOR_SECTION);
                row.getElement().getStyle().setProperty("borderLeft",
                    "3px solid " + (stepPassed ? "#16a34a" : "#dc2626"));

                Label icon = new Label(stepPassed ? "✓" : "✗");
                icon.getElement().getStyle().setProperty("color", stepPassed ? "#16a34a" : "#dc2626");
                icon.getElement().getStyle().setProperty("fontWeight", "700");
                icon.getElement().getStyle().setProperty("minWidth", "16px");
                icon.getElement().getStyle().setProperty("fontSize", "12px");

                Label name = new Label(sid);
                name.getElement().getStyle().setProperty("flex", "1");
                name.getElement().getStyle().setProperty("fontSize", "12px");
                name.getElement().getStyle().setProperty("color", Theme.COLOR_TEXT);

                Label meta = new Label(scode + "  " + sdur + " ms");
                meta.getElement().getStyle().setProperty("fontSize", "11px");
                meta.getElement().getStyle().setProperty("color", Theme.COLOR_MUTED);
                meta.getElement().getStyle().setProperty("fontFamily", "monospace");

                row.add(icon);
                row.add(name);
                row.add(meta);
                stepList.add(row);
            }
            card.add(stepList);
        }

        Button viewBtn = UiFactory.outlineButton("View Full Report ↗");
        viewBtn.getElement().getStyle().setProperty("cursor", "pointer");
        viewBtn.getElement().getStyle().setProperty("color", Theme.COLOR_TEMPLATE);
        viewBtn.addClickHandler(e -> openReportInNewTab(reportUrl));
        card.add(viewBtn);

        overlay.add(card);
        state.root.add(overlay);
    }

    public FlowPanel statBadge(String label, String value, String color) {
        FlowPanel p = new FlowPanel();
        p.getElement().getStyle().setProperty("background", Theme.COLOR_SECTION);
        p.getElement().getStyle().setProperty("border", "1px solid " + Theme.COLOR_BORDER);
        p.getElement().getStyle().setProperty("borderTop", "3px solid " + color);
        p.getElement().getStyle().setProperty("borderRadius", "8px");
        p.getElement().getStyle().setProperty("padding", "10px 8px");
        p.getElement().getStyle().setProperty("textAlign", "center");
        Label v = new Label(value);
        v.getElement().getStyle().setProperty("fontWeight", "700");
        v.getElement().getStyle().setProperty("color", color);
        v.getElement().getStyle().setProperty("fontSize", "18px");
        Label l = new Label(label);
        l.getElement().getStyle().setProperty("fontSize", "10px");
        l.getElement().getStyle().setProperty("color", Theme.COLOR_MUTED);
        p.add(v);
        p.add(l);
        return p;
    }

    public static native void openReportInNewTab(String url) /*-{
        $wnd.open(url, '_blank');
    }-*/;

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
