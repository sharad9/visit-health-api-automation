package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.model.CheckData;
import com.visit.gwtapiflowbuilder.client.model.StepData;
import com.visit.gwtapiflowbuilder.client.service.StepRunner;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

import java.util.List;

/**
 * Builds the Steps section UI: step cards, extract/check/response sub-sections.
 */
public final class StepSection {

    private final AppState state;
    private final FormBuilder formBuilder;
    private final TokenRenderer tokenRenderer;
    private final StepRunner stepRunner;

    public StepSection(AppState state, FormBuilder formBuilder, TokenRenderer tokenRenderer, StepRunner stepRunner) {
        this.state = state;
        this.formBuilder = formBuilder;
        this.tokenRenderer = tokenRenderer;
        this.stepRunner = stepRunner;
    }

    // -------------------------------------------------------------------------
    // Build steps body
    // -------------------------------------------------------------------------

    public FlowPanel build() {
        FlowPanel body = new FlowPanel();
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        body.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_10);

        FlowPanel stepsList = new FlowPanel();
        stepsList.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        stepsList.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_10);

        for (StepData data : state.stepsData) {
            stepsList.add(stepCard(data));
        }

        Button addStepButton = UiFactory.outlineButton("＋ Add Step");
        addStepButton.addClickHandler(event -> {
            stepsList.add(stepCard(new StepData("STEP_" + (state.stepCounter + 1), "GET", "{{BASE_URL}}/resource")));
            if (state.onUpdate != null) state.onUpdate.run();
        });

        body.add(stepsList);
        body.add(addStepButton);
        return body;
    }

    // -------------------------------------------------------------------------
    // Step card
    // -------------------------------------------------------------------------

    public FlowPanel stepCard(StepData data) {
        FlowPanel step = new FlowPanel();
        step.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        step.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "12px");
        step.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);
        step.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        step.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 1px 2px rgba(15,23,42,0.06)");
        step.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        step.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        StepBlock block = new StepBlock();
        int stepNumber = ++state.stepCounter;
        String stepIdValue = (data.stepId == null || data.stepId.isEmpty()) ? "STEP_" + stepNumber : data.stepId;
        block.stepId = formBuilder.textBox(stepIdValue, "STEP_ID");

        // Header row
        FlowPanel headerRow = new FlowPanel();
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "6px 8px");
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        headerRow.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_BOTTOM, "1px solid " + Theme.COLOR_BORDER);

        FlowPanel headerLeft = new FlowPanel();
        headerLeft.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        headerLeft.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        headerLeft.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "6px");

        Label headerLabel = new Label("Step " + stepNumber);
        headerLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        headerLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_700);
        headerLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);

        Label methodBadge = new Label(data.method == null ? "GET" : data.method);
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "2px 6px");
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "10px");
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PRIMARY_LIGHT);
        methodBadge.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_PRIMARY);
        headerLeft.add(headerLabel);
        headerLeft.add(methodBadge);
        headerRow.add(headerLeft);

        // Step body
        FlowPanel stepBody = new FlowPanel();
        stepBody.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        stepBody.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);
        stepBody.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_SECTION_BODY);

        Button toggleStepButton = UiFactory.ghostButton("Close");
        toggleStepButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        toggleStepButton.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        toggleStepButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        toggleStepButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        toggleStepButton.addClickHandler(event -> {
            boolean isOpen = !BaseStyle.Value.NONE.equals(
                stepBody.getElement().getStyle().getProperty(BaseStyle.Key.DISPLAY));
            stepBody.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY,
                isOpen ? BaseStyle.Value.NONE : BaseStyle.Value.GRID);
            toggleStepButton.setText(isOpen ? "Open" : "Close");
        });

        Button removeStepButton = UiFactory.removeButton("✖");
        removeStepButton.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "28px");
        removeStepButton.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "24px");
        removeStepButton.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "0");
        removeStepButton.addClickHandler(event -> {
            int index = state.steps.indexOf(block);
            if (index >= 0) {
                state.steps.remove(index);
                if (index < state.stepsData.size()) {
                    state.stepsData.remove(index);
                }
            }
            if (step.getParent() instanceof FlowPanel) {
                ((FlowPanel) step.getParent()).remove(step);
            }
            if (state.onUpdate != null) state.onUpdate.run();
        });

        FlowPanel headerRight = new FlowPanel();
        headerRight.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        headerRight.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        headerRight.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "6px");
        Button runStepButton = UiFactory.ghostButton("▶ Run");
        runStepButton.addClickHandler(event -> stepRunner.runStep(block, runStepButton));
        headerRight.add(runStepButton);
        headerRight.add(toggleStepButton);
        headerRight.add(removeStepButton);
        headerRow.add(headerRight);
        step.add(headerRow);
        step.add(stepBody);

        // Step identifier
        FlowPanel stepIdentifierRow = formBuilder.row();
        stepIdentifierRow.add(UiFactory.field("Step Identifier", tokenRenderer.previewToggle(block.stepId)));
        stepBody.add(stepIdentifierRow);

        // Method + URL
        FlowPanel methodUrlRow = formBuilder.row("140px 1fr");
        block.method = formBuilder.select("GET", "POST", "PUT", "DELETE");
        block.method.setSelectedIndex(formBuilder.indexOf(block.method, data.method));
        block.url = formBuilder.textBoxNoPreview(data.url, "{{BASE_URL}}/resource");
        methodUrlRow.add(UiFactory.field("HTTP Method", block.method));

        FlowPanel urlFieldContainer = new FlowPanel();
        urlFieldContainer.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        urlFieldContainer.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "1fr");
        urlFieldContainer.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        urlFieldContainer.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.FLEX_START);

        FlowPanel urlInputWrap = UiFactory.field("Request URL", block.url);
        urlInputWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");

        FlowPanel urlPreviewWrap = new FlowPanel();
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_INPUT);
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "28px");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "28px");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SIZING, "border-box");
        urlPreviewWrap.getElement().getStyle().setProperty(BaseStyle.Key.LINE_HEIGHT, "normal");
        urlPreviewWrap.add(tokenRenderer.tokenPreview(block.url));

        FlowPanel urlPreviewField = UiFactory.field("Request URL", urlPreviewWrap);
        urlPreviewField.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");

        urlInputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
        urlPreviewField.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);

        urlPreviewWrap.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                urlInputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
                urlPreviewField.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                block.url.setFocus(true);
            }
        }, DoubleClickEvent.getType());

        block.url.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == 13) {
                    urlInputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                    urlPreviewField.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
                }
            }
        });

        block.url.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                urlInputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                urlPreviewField.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
            }
        });

        urlFieldContainer.add(urlInputWrap);
        urlFieldContainer.add(urlPreviewField);
        methodUrlRow.add(urlFieldContainer);
        stepBody.add(methodUrlRow);

        // Retry/timeout
        FlowPanel retryConfigRow = formBuilder.row();
        block.timeoutMs = formBuilder.numberBox(emptyIfNull(data.timeoutMs, "10000"));
        block.retryCount = formBuilder.numberBox(emptyIfNull(data.retryCount, "2"));
        block.retryDelay = formBuilder.numberBox(emptyIfNull(data.retryDelay, "500"));
        retryConfigRow.add(UiFactory.field("Timeout (ms)", tokenRenderer.previewToggle(block.timeoutMs)));
        retryConfigRow.add(UiFactory.field("Retry Count", tokenRenderer.previewToggle(block.retryCount)));
        retryConfigRow.add(UiFactory.field("Retry Delay", tokenRenderer.previewToggle(block.retryDelay)));
        stepBody.add(retryConfigRow);

        stepBody.add(formBuilder.keyValueSection("Headers", block.headers, data.headers));
        stepBody.add(formBuilder.keyValueSection("Request Variables", block.requestVariables, data.requestVariables));
        stepBody.add(formBuilder.keyValueSection("Body", block.body, data.body));
        stepBody.add(extractSection(block, data));
        stepBody.add(checkSection(block, data.checks));
        stepBody.add(responseSection(block));

        Label hint = new Label("Use tokens like {{BASE_URL}} and {{ORDER_ID}}");
        hint.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
        hint.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        stepBody.add(hint);

        formBuilder.bindUpdate(block.stepId);
        formBuilder.bindUpdate(block.method);
        formBuilder.bindUpdate(block.url);
        formBuilder.bindUpdate(block.timeoutMs);
        formBuilder.bindUpdate(block.retryCount);
        formBuilder.bindUpdate(block.retryDelay);

        state.steps.add(block);
        if (state.onUpdate != null) state.onUpdate.run();
        return step;
    }

    // -------------------------------------------------------------------------
    // Sub-sections
    // -------------------------------------------------------------------------

    public FlowPanel extractSection(StepBlock block, StepData data) {
        FlowPanel section = new FlowPanel();
        section.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        section.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        section.add(UiFactory.smallLabel("Extraction"));
        section.add(formBuilder.keyValueSection("Body JSON Paths", block.extractBody, data.extractBody));
        section.add(formBuilder.keyValueSection("Header Values", block.extractHeaders, data.extractHeaders));
        return section;
    }

    public FlowPanel checkSection(StepBlock block, List<CheckData> checksData) {
        FlowPanel section = new FlowPanel();
        section.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        section.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        section.add(UiFactory.smallLabel("Checks"));

        FlowPanel checkRowsContainer = new FlowPanel();
        checkRowsContainer.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        checkRowsContainer.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        section.add(checkRowsContainer);

        Button addCheckButton = UiFactory.ghostButton("＋ Add Check");
        addCheckButton.addClickHandler(event -> {
            CheckRow checkRow = newCheckRow(new CheckData("status", "", "", false));
            block.checks.add(checkRow);
            checkRowsContainer.add(checkRow.container());
            if (state.onUpdate != null) state.onUpdate.run();
        });
        section.add(addCheckButton);

        if (checksData == null || checksData.isEmpty()) {
            CheckRow initialCheckRow = newCheckRow(new CheckData("status", "", "", false));
            block.checks.add(initialCheckRow);
            checkRowsContainer.add(initialCheckRow.container());
        } else {
            for (CheckData checkData : checksData) {
                CheckRow checkRow = newCheckRow(checkData);
                block.checks.add(checkRow);
                checkRowsContainer.add(checkRow.container());
            }
        }
        return section;
    }

    public FlowPanel responseSection(StepBlock block) {
        FlowPanel section = new FlowPanel();
        section.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        section.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        section.add(UiFactory.smallLabel("Last Response"));

        Label responseDisplayLabel = new Label();
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "120px");
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_MONO);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.WHITE_SPACE, BaseStyle.Value.PRE_WRAP);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW_WRAP, "break-word");
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.WORD_BREAK, "break-all");
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_INPUT);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1.5px solid " + Theme.COLOR_BORDER);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        responseDisplayLabel.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SIZING, "border-box");
        section.add(responseDisplayLabel);
        block.runResponse = responseDisplayLabel;
        return section;
    }

    public CheckRow newCheckRow(CheckData data) {
        ListBox source = formBuilder.select("status", "body", "headers");
        source.setSelectedIndex(formBuilder.indexOf(source, data.source));
        TextBox path = formBuilder.textBox(emptyIfNull(data.jsonPath, ""), "$.path");
        TextBox equals = formBuilder.textBox(emptyIfNull(data.equalsValue, ""), "equals");
        CheckBox exists = new CheckBox("Exists");
        exists.setValue(data.exists);
        exists.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        formBuilder.bindUpdate(source);
        formBuilder.bindUpdate(exists);
        FlowPanel pathWrap = tokenRenderer.previewToggle(path);
        FlowPanel equalsWrap = tokenRenderer.previewToggle(equals);
        return new CheckRow(source, path, pathWrap, equals, equalsWrap, exists);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String emptyIfNull(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value;
    }
}
