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
        Button closeBtn = UiFactory.ghostButton("✕");
        closeBtn.addClickHandler(e -> state.root.remove(overlay));
        titleRow.add(title);
        titleRow.add(closeBtn);
        card.add(titleRow);

        // Executed at
        Label atLabel = new Label("Executed: " + execAt);
        atLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        atLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
        card.add(atLabel);

        // Stats row
        FlowPanel statsRow = new FlowPanel();
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "repeat(4,1fr)");
        statsRow.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "8px");
        statsRow.add(statBadge("Total",    String.valueOf(total),  "#2563eb"));
        statsRow.add(statBadge("Passed",   String.valueOf(passed), "#16a34a"));
        statsRow.add(statBadge("Failed",   String.valueOf(failed), failed > 0 ? "#dc2626" : "#16a34a"));
        statsRow.add(statBadge("Duration", ms + " ms",             "#7c3aed"));
        card.add(statsRow);

        // Step list
        JSONValue stepsVal = obj.get("steps");
        JSONArray stepsArr = stepsVal == null ? null : stepsVal.isArray();
        if (stepsArr != null) {
            FlowPanel stepList = new FlowPanel();
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "4px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.MAX_HEIGHT, "220px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW_Y, BaseStyle.Value.AUTO);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "4px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
            stepList.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_BG);

            for (int i = 0; i < stepsArr.size(); i++) {
                JSONObject s = stepsArr.get(i).isObject();
                if (s == null) continue;
                boolean stepPassed = s.get("passed").isBoolean().booleanValue();
                String sid  = s.get("stepIdentifier").isString().stringValue();
                long sdur   = (long) s.get("durationMs").isNumber().doubleValue();
                int scode   = (int) s.get("statusCode").isNumber().doubleValue();

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
                icon.getElement().getStyle().setProperty(BaseStyle.Key.COLOR,       stepPassed ? "#16a34a" : "#dc2626");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, "700");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH,   "16px");
                icon.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE,   "12px");

                Label name = new Label(sid);
                name.getElement().getStyle().setProperty(BaseStyle.Key.FLEX,      "1");
                name.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "12px");
                name.getElement().getStyle().setProperty(BaseStyle.Key.COLOR,     Theme.COLOR_TEXT);

                Label meta = new Label(scode + "  " + sdur + " ms");
                meta.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE,   "11px");
                meta.getElement().getStyle().setProperty(BaseStyle.Key.COLOR,       Theme.COLOR_MUTED);
                meta.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_MONO);

                row.add(icon);
                row.add(name);
                row.add(meta);
                stepList.add(row);
            }
            card.add(stepList);
        }

        // View report button
        Button viewBtn = UiFactory.outlineButton("View Full Report ↗");
        viewBtn.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        viewBtn.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEMPLATE);
        viewBtn.addClickHandler(e -> openReportInNewTab(reportUrl));
        card.add(viewBtn);

        overlay.add(card);
        state.root.add(overlay);
    }

    // -------------------------------------------------------------------------
    // Stat badge
    // -------------------------------------------------------------------------

    public FlowPanel statBadge(String label, String value, String color) {
        FlowPanel p = new FlowPanel();
        p.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        p.getElement().getStyle().setProperty(BaseStyle.Key.BORDER,           "1px solid " + Theme.COLOR_BORDER);
        p.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_TOP,       "3px solid " + color);
        p.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS,    "8px");
        p.getElement().getStyle().setProperty(BaseStyle.Key.PADDING,          "10px 8px");
        p.getElement().getStyle().setProperty(BaseStyle.Key.TEXT_ALIGN,       BaseStyle.Value.CENTER);

        Label v = new Label(value);
        v.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, "700");
        v.getElement().getStyle().setProperty(BaseStyle.Key.COLOR,       color);
        v.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE,   "18px");

        Label l = new Label(label);
        l.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, "10px");
        l.getElement().getStyle().setProperty(BaseStyle.Key.COLOR,     Theme.COLOR_MUTED);

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
