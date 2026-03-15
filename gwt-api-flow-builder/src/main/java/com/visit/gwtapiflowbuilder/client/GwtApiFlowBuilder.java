package com.visit.gwtapiflowbuilder.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.model.StepData;
import com.visit.gwtapiflowbuilder.client.service.FlowPersistenceService;
import com.visit.gwtapiflowbuilder.client.service.FlowSyncService;
import com.visit.gwtapiflowbuilder.client.service.StepRunner;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;
import com.visit.gwtapiflowbuilder.client.ui.CollectionRunnerView;
import com.visit.gwtapiflowbuilder.client.ui.EnvironmentSection;
import com.visit.gwtapiflowbuilder.client.ui.FormBuilder;
import com.visit.gwtapiflowbuilder.client.ui.NavbarView;
import com.visit.gwtapiflowbuilder.client.ui.PreviewPanelView;
import com.visit.gwtapiflowbuilder.client.ui.SectionView;
import com.visit.gwtapiflowbuilder.client.ui.TokenRenderer;
import com.visit.gwtapiflowbuilder.client.ui.UiFactory;

/**
 * Entry point — thin coordinator that wires AppState, services, and UI sections together.
 */
public class GwtApiFlowBuilder implements EntryPoint {

    private final AppState state = new AppState();

    // Services created once; UI sections recreated on each render
    private FlowSyncService syncService;
    private FlowPersistenceService persistenceService;
    private CollectionRunnerView collectionRunner;

    @Override
    public void onModuleLoad() {
        Theme.setMode(false);
        syncService = new FlowSyncService(state);
        persistenceService = new FlowPersistenceService(state);

        // Wire callbacks
        state.onUpdate = () -> syncService.updateJson();
        state.onRender = () -> render();
        state.onFullReset = () -> { resetState(); render(); };
        state.onLoadComplete = json -> syncService.applyJsonText(json, false);

        resetState();
        render();
        persistenceService.loadFlow();
    }

    // -------------------------------------------------------------------------
    // Full render — recreates all UI; called on load and after JSON parse
    // -------------------------------------------------------------------------

    private void render() {
        state.root = RootPanel.get("gwt-root");
        if (state.root == null) {
            state.root = RootPanel.get();
        }
        state.root.clear();

        RootPanel.get().getElement().getStyle().setProperty(BaseStyle.Key.MARGIN, "0");
        RootPanel.get().getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "100%");
        RootPanel.get().getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_BASE);
        RootPanel.get().getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_BG);
        RootPanel.get().getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);

        FlowPanel app = new FlowPanel();
        app.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        app.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_DIRECTION, BaseStyle.Value.COLUMN);
        app.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "100vh");
        app.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);

        // Build fresh section instances using current AppState
        TokenRenderer tokenRenderer = new TokenRenderer(state);
        FormBuilder formBuilder = new FormBuilder(state, tokenRenderer);
        StepRunner stepRunner = new StepRunner(state, tokenRenderer);
        EnvironmentSection envSection = new EnvironmentSection(state, formBuilder, tokenRenderer);
        com.visit.gwtapiflowbuilder.client.ui.StepSection stepSection =
            new com.visit.gwtapiflowbuilder.client.ui.StepSection(state, formBuilder, tokenRenderer, stepRunner);
        collectionRunner = new CollectionRunnerView(state);

        // Flow select dropdown
        state.flowSelect = new ListBox();
        state.flowSelect.addItem("Load flow...");
        state.flowSelect.addChangeHandler(event -> {
            int idx = state.flowSelect.getSelectedIndex();
            if (idx <= 0) return;
            String name = state.flowSelect.getItemText(idx);
            persistenceService.loadFlowByName(name);
        });

        // Navbar
        state.navbarView = new NavbarView();
        app.add(state.navbarView.build(
            toggleHandler(), Theme.toggleLabel(),
            saveHandler(),
            event -> collectionRunner.runCollection(),
            state.flowSelect));

        // Content row (form | divider | preview)
        state.contentRow = new FlowPanel();
        state.contentRow.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        state.contentRow.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "1");
        state.contentRow.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "0");
        state.contentRow.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);

        state.formPanel = new FlowPanel();
        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "1");
        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW_Y, BaseStyle.Value.AUTO);
        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);

        FlowPanel formInner = new FlowPanel();
        formInner.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        formInner.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_10);
        formInner.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_FORM_INNER);
        formInner.add(new SectionView("Meta", metaBody(formBuilder, tokenRenderer), true).build());
        formInner.add(new SectionView("Environment", envSection.build(), true).build());
        formInner.add(new SectionView("Global Inputs", inputsBody(formBuilder), true).build());
        formInner.add(new SectionView("Steps", stepSection.build(), true).build());
        state.formPanel.add(formInner);

        state.contentRow.add(state.formPanel);

        state.divider = new FlowPanel();
        state.divider.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "6px");
        state.divider.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "6px");
        state.divider.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, "col-resize");
        state.divider.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        state.divider.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_SHRINK, "0");
        state.contentRow.add(state.divider);

        PreviewPanelView previewPanel = new PreviewPanelView();
        state.jsonArea = previewPanel.getJsonArea();
        state.previewPanel = previewPanel;
        state.previewPanelWrap = previewPanel.getPanel();
        state.contentRow.add(state.previewPanelWrap);

        app.add(state.contentRow);
        state.root.add(app);

        envSection.loadEnvironmentIntoUi(state.environments.get(state.activeEnvIndex));
        syncService.updateJson();
        tokenRenderer.ensureTokenTooltip();
        bindResizeHandlers();
        syncService.bindWindowCloseWarning();
        syncService.bindJsonSync();
        persistenceService.loadFlowList();
    }

    // -------------------------------------------------------------------------
    // Meta section
    // -------------------------------------------------------------------------

    private FlowPanel metaBody(FormBuilder formBuilder, TokenRenderer tokenRenderer) {
        FlowPanel body = new FlowPanel();
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        body.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);
        FlowPanel row = UiFactory.row("1.2fr 0.6fr");
        state.metaIdBox = formBuilder.textBox(state.metaIdValue, "workflow.id");
        state.metaVersionBox = formBuilder.textBox(state.metaVersionValue, "1.0");
        row.add(UiFactory.field("API Flow ID", tokenRenderer.previewToggle(state.metaIdBox)));
        row.add(UiFactory.field("Version", tokenRenderer.previewToggle(state.metaVersionBox)));
        body.add(row);
        return body;
    }

    // -------------------------------------------------------------------------
    // Global inputs section
    // -------------------------------------------------------------------------

    private FlowPanel inputsBody(FormBuilder formBuilder) {
        FlowPanel body = new FlowPanel();
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        body.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        FlowPanel list = new FlowPanel();
        list.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        list.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        formBuilder.populateKeyValueRows(list, state.globalInputRows, state.globalInputsData, "KEY", "{{VARIABLE}}");
        body.add(list);

        com.google.gwt.user.client.ui.Button add = UiFactory.ghostButton("＋ Add Input");
        add.addClickHandler(event -> {
            formBuilder.addKeyValueRow(list, state.globalInputRows, "", "", "KEY", "{{VARIABLE}}");
            if (state.onUpdate != null) state.onUpdate.run();
        });
        body.add(add);
        return body;
    }

    // -------------------------------------------------------------------------
    // Navbar handlers
    // -------------------------------------------------------------------------

    private ClickHandler toggleHandler() {
        return event -> {
            Theme.setMode(!Theme.IS_DARK);
            syncService.applyJsonText(state.jsonArea.getText(), false);
        };
    }

    private ClickHandler saveHandler() {
        return event -> persistenceService.saveFlow(state.jsonArea.getText());
    }

    // -------------------------------------------------------------------------
    // State initialisation
    // -------------------------------------------------------------------------

    private void resetState() {
        state.environments.clear();
        state.envVarRows.clear();
        state.globalInputRows.clear();
        state.steps.clear();
        state.globalInputsData.clear();
        state.stepsData.clear();
        state.stepCounter = 0;
        state.activeEnvIndex = 0;
        state.envCounter = 0;
        state.metaIdValue = AppState.META_ID;
        state.metaVersionValue = AppState.META_VERSION;
        seedEnvironments();
        seedDefaults();
    }

    private void seedDefaults() {
        state.globalInputsData.add(new KeyValuePair("USER_PHONE", "{{USER_PHONE}}"));
        state.globalInputsData.add(new KeyValuePair("USER_OTP", "{{USER_OTP}}"));
        state.stepsData.add(new StepData("STEP_1", "GET", "{{BASE_URL}}/users/{{USER_ID}}"));
        state.stepsData.add(new StepData("STEP_2", "POST", "{{BASE_URL}}/orders/{{ORDER_ID}}/events"));
    }

    private void seedEnvironments() {
        state.environments.add(new EnvironmentItem("1", "STAGE",
                "https://stage.api.example.com", "10000", "2", "500",
                new KeyValuePair("BASE_URL", "https://stage.api.example.com"),
                new KeyValuePair("TENANT_ID", "stage-tenant")));
        state.environments.add(new EnvironmentItem("2", "UAT",
                "https://uat.api.example.com", "10000", "2", "500",
                new KeyValuePair("BASE_URL", "https://uat.api.example.com"),
                new KeyValuePair("TENANT_ID", "uat-tenant")));
        state.environments.add(new EnvironmentItem("3", "PROD",
                "https://api.example.com", "10000", "2", "500",
                new KeyValuePair("BASE_URL", "https://api.example.com"),
                new KeyValuePair("TENANT_ID", "prod-tenant")));
        state.envCounter = Math.max(state.envCounter, state.environments.size());
    }

    // -------------------------------------------------------------------------
    // Resize handlers
    // -------------------------------------------------------------------------

    private void bindResizeHandlers() {
        state.divider.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                state.resizing = true;
                state.resizeStartX = event.getClientX();
                state.resizeStartLeft = state.formPanel.getOffsetWidth();
                event.preventDefault();
            }
        }, MouseDownEvent.getType());

        state.contentRow.addDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (!state.resizing) return;
                int delta = event.getClientX() - state.resizeStartX;
                applySplitSizes(state.resizeStartLeft + delta);
            }
        }, MouseMoveEvent.getType());

        state.contentRow.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                state.resizing = false;
            }
        }, MouseUpEvent.getType());

        if (state.leftWidthPx <= 0) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    int total = state.contentRow.getOffsetWidth();
                    if (total > 0) {
                        state.leftWidthPx = (int) (total * 0.58);
                        applySplitSizes(state.leftWidthPx);
                    }
                }
            });
        } else {
            applySplitSizes(state.leftWidthPx);
        }
    }

    private void applySplitSizes(int requestedLeftWidth) {
        int dividerWidth = 6;
        int total = state.contentRow.getOffsetWidth();
        if (total <= 0) return;
        int minLeft = 360;
        int minRight = 360;
        int maxLeft = Math.max(minLeft, total - minRight - dividerWidth);
        int left = Math.max(minLeft, Math.min(maxLeft, requestedLeftWidth));
        int right = Math.max(minRight, total - left - dividerWidth);
        state.leftWidthPx = left;

        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, left + "px");
        state.formPanel.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "0 0 auto");
        state.previewPanelWrap.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, right + "px");
        state.previewPanelWrap.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "0 0 auto");
    }
}
