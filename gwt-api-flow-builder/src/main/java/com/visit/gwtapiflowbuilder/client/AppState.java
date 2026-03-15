package com.visit.gwtapiflowbuilder.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.visit.gwtapiflowbuilder.client.model.EnvironmentItem;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.model.StepData;
import com.visit.gwtapiflowbuilder.client.ui.KeyValueRow;
import com.visit.gwtapiflowbuilder.client.ui.NavbarView;
import com.visit.gwtapiflowbuilder.client.ui.PreviewPanelView;
import com.visit.gwtapiflowbuilder.client.ui.StepBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central shared state passed to all components via constructor injection.
 * Components read/write state directly and call onUpdate.run() to trigger JSON sync,
 * or onRender.run() to trigger a full re-render.
 */
public final class AppState {

    public static final String META_ID = "default.flow.meta";
    public static final String META_VERSION = "1.0";
    public static final String DEFAULT_FLOW_NAME = "default.flow.meta";

    // -------------------------------------------------------------------------
    // Data collections
    // -------------------------------------------------------------------------

    public final List<EnvironmentItem> environments = new ArrayList<>();
    public final List<StepBlock> steps = new ArrayList<>();
    public final List<KeyValuePair> globalInputsData = new ArrayList<>();
    public final List<StepData> stepsData = new ArrayList<>();
    public final Map<String, String> runtimeVariables = new LinkedHashMap<>();
    public final List<KeyValueRow> envVarRows = new ArrayList<>();
    public final List<KeyValueRow> globalInputRows = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Counters and indexes
    // -------------------------------------------------------------------------

    public int activeEnvIndex = 0;
    public int stepCounter = 0;
    public int envCounter = 0;

    // -------------------------------------------------------------------------
    // String state
    // -------------------------------------------------------------------------

    public String metaIdValue = META_ID;
    public String metaVersionValue = META_VERSION;
    public String lastSavedJson = "";
    public String lastSavedName = DEFAULT_FLOW_NAME;

    // -------------------------------------------------------------------------
    // Flags
    // -------------------------------------------------------------------------

    public boolean suppressJsonUpdates = false;
    public boolean suppressJsonParse = false;
    public boolean dirty = false;

    // -------------------------------------------------------------------------
    // Resize state
    // -------------------------------------------------------------------------

    public boolean resizing = false;
    public int resizeStartX = 0;
    public int resizeStartLeft = 0;
    public int leftWidthPx = -1;

    // -------------------------------------------------------------------------
    // UI element references (set during render)
    // -------------------------------------------------------------------------

    public RootPanel root;
    public TextArea jsonArea;
    public TextBox metaIdBox;
    public TextBox metaVersionBox;
    public ListBox envSelect;
    public TextBox envNameBox;
    public FlowPanel envNamePreview;
    public TextBox timeoutBox;
    public TextBox retryCountBox;
    public TextBox retryDelayBox;
    public FlowPanel envVarsList;
    public FlowPanel contentRow;
    public FlowPanel formPanel;
    public FlowPanel divider;
    public FlowPanel previewPanelWrap;
    public PreviewPanelView previewPanel;
    public ListBox flowSelect;
    public NavbarView navbarView;
    public Element tokenTooltip;

    // -------------------------------------------------------------------------
    // Timers
    // -------------------------------------------------------------------------

    public Timer jsonSyncTimer;

    // -------------------------------------------------------------------------
    // Callbacks set by the coordinator
    // -------------------------------------------------------------------------

    /** Called by any component when data changes → triggers FlowSyncService.updateJson() */
    public Runnable onUpdate;

    /** Called by FlowSyncService when full re-render is needed (e.g. after JSON parse) */
    public Runnable onRender;

    /** Called by FlowSyncService when parse fails on a server-loaded flow — resets and re-renders */
    public Runnable onFullReset;

    /** Called by FlowPersistenceService when a flow is loaded from the server */
    public StringConsumer onLoadComplete;
}
