package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

/**
 * Handles token preview rendering and tooltip display.
 * Resolves {{TOKEN}} values from AppState (envVarRows, globalInputRows, runtimeVariables).
 */
public final class TokenRenderer {

    private final AppState state;

    public TokenRenderer(AppState state) {
        this.state = state;
    }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    public void ensureTokenTooltip() {
        if (state.tokenTooltip != null) {
            return;
        }
        Element tooltipElement = Document.get().createDivElement();
        tooltipElement.getStyle().setProperty(BaseStyle.Key.POSITION, "absolute");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.Z_INDEX, "9999");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, "#1f1f1f");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.COLOR, "#F8FAFC");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid rgba(255,255,255,0.08)");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "10px");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.PADDING, "10px 12px");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 10px 30px rgba(0,0,0,0.35)");
        tooltipElement.getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_MONO);
        tooltipElement.getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        tooltipElement.getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
        tooltipElement.getStyle().setProperty(BaseStyle.Key.POINTER_EVENTS, BaseStyle.Value.NONE);
        RootPanel.get().getElement().appendChild(tooltipElement);
        state.tokenTooltip = tooltipElement;
    }

    public void showTokenTooltip(String token, Element target) {
        ensureTokenTooltip();
        String value = resolveTokenValue(token);
        state.tokenTooltip.setInnerText(value.isEmpty() ? "Not set" : value);
        int left = target.getAbsoluteLeft();
        int top = target.getAbsoluteTop() + target.getOffsetHeight() + 8;
        state.tokenTooltip.getStyle().setProperty(BaseStyle.Key.LEFT, left + "px");
        state.tokenTooltip.getStyle().setProperty(BaseStyle.Key.TOP, top + "px");
        state.tokenTooltip.getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.BLOCK);
    }

    public void hideTokenTooltip() {
        if (state.tokenTooltip == null) {
            return;
        }
        state.tokenTooltip.getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
    }

    // -------------------------------------------------------------------------
    // Token resolution
    // -------------------------------------------------------------------------

    public String resolveTokenValue(String token) {
        if (token == null) {
            return "";
        }
        String key = token;
        if (key.startsWith("{{") && key.endsWith("}}")) {
            key = key.substring(2, key.length() - 2).trim();
        }
        if (key.isEmpty()) {
            return "";
        }
        for (KeyValueRow row : state.envVarRows) {
            if (key.equals(row.key.getValue())) {
                return emptyIfNull(row.value.getValue(), "");
            }
        }
        for (KeyValueRow row : state.globalInputRows) {
            if (key.equals(row.key.getValue())) {
                return emptyIfNull(row.value.getValue(), "");
            }
        }
        if (state.runtimeVariables.containsKey(key)) {
            return state.runtimeVariables.get(key);
        }
        return "";
    }

    public String replaceTokens(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder resolvedText = new StringBuilder();
        int parseIndex = 0;
        while (parseIndex < text.length()) {
            int start = text.indexOf("{{", parseIndex);
            if (start < 0) {
                resolvedText.append(text.substring(parseIndex));
                break;
            }
            int end = text.indexOf("}}", start + 2);
            if (end < 0) {
                resolvedText.append(text.substring(parseIndex));
                break;
            }
            resolvedText.append(text.substring(parseIndex, start));
            String token = text.substring(start, end + 2);
            resolvedText.append(resolveTokenValue(token));
            parseIndex = end + 2;
        }
        return resolvedText.toString();
    }

    // -------------------------------------------------------------------------
    // Preview rendering
    // -------------------------------------------------------------------------

    public void renderTokenPreview(FlowPanel preview, String text) {
        preview.clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf("{{", index);
            if (start < 0) {
                preview.add(plainLabel(text.substring(index)));
                break;
            }
            if (start > index) {
                preview.add(plainLabel(text.substring(index, start)));
            }
            int end = text.indexOf("}}", start + 2);
            if (end < 0) {
                preview.add(plainLabel(text.substring(start)));
                break;
            }
            preview.add(tokenLabel(text.substring(start, end + 2)));
            index = end + 2;
        }
    }

    public FlowPanel tokenPreview(TextBox source) {
        FlowPanel preview = new FlowPanel();
        preview.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        preview.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_WRAP, BaseStyle.Value.WRAP);
        preview.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "2px");
        preview.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_82);
        preview.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        preview.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        preview.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        preview.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW_WRAP, "break-word");
        preview.getElement().getStyle().setProperty(BaseStyle.Key.WORD_BREAK, "break-all");
        renderTokenPreview(preview, source.getValue());
        Scheduler.get().scheduleDeferred(() -> renderTokenPreview(preview, source.getValue()));
        source.addKeyUpHandler(event -> renderTokenPreview(preview, source.getValue()));
        return preview;
    }

    /**
     * Standard toggle (double-click preview to edit, blur/Enter to return to preview).
     */
    public FlowPanel previewToggle(TextBox source) {
        FlowPanel wrap = buildWrap();

        FlowPanel inputWrap = buildInputWrap(source);
        FlowPanel previewWrap = buildPreviewWrap(tokenPreview(source));

        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);

        previewWrap.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
                previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                source.setFocus(true);
                source.selectAll();
            }
        }, DoubleClickEvent.getType());

        source.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == 13) {
                    inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                    previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
                }
            }
        });

        source.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
            }
        });

        wrap.add(inputWrap);
        wrap.add(previewWrap);
        return wrap;
    }

    /**
     * Toggle with an existing preview panel and a commit callback (used for envNameBox).
     * Single-click to edit; onCommit.run() called on blur and Enter.
     */
    public FlowPanel previewToggle(TextBox source, FlowPanel existingTokenPreview, Runnable onCommit) {
        FlowPanel wrap = buildWrap();

        FlowPanel inputWrap = buildInputWrap(source);
        FlowPanel previewWrap = buildPreviewWrap(existingTokenPreview);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);

        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);

        previewWrap.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                source.setReadOnly(false);
                inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
                previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                source.setFocus(true);
                source.selectAll();
            }
        }, ClickEvent.getType());

        source.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == 13) {
                    if (onCommit != null) onCommit.run();
                    inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                    previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
                }
            }
        });

        source.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if (onCommit != null) onCommit.run();
                inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.NONE);
                previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
            }
        });

        wrap.add(inputWrap);
        wrap.add(previewWrap);
        return wrap;
    }

    // -------------------------------------------------------------------------
    // Label factories
    // -------------------------------------------------------------------------

    public Label plainLabel(String text) {
        Label label = new Label(text);
        label.getElement().getStyle().setProperty(BaseStyle.Key.WHITE_SPACE, BaseStyle.Value.PRE_WRAP);
        return label;
    }

    public Label tokenLabel(String token) {
        Label label = new Label(token);
        label.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEMPLATE);
        label.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        label.getElement().getStyle().setProperty(BaseStyle.Key.WHITE_SPACE, BaseStyle.Value.PRE_WRAP);
        label.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                showTokenTooltip(token, label.getElement());
            }
        }, MouseOverEvent.getType());
        label.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                hideTokenTooltip();
            }
        }, MouseOutEvent.getType());
        return label;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    public String emptyIfNull(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    // -------------------------------------------------------------------------
    // Private layout helpers
    // -------------------------------------------------------------------------

    private FlowPanel buildWrap() {
        FlowPanel wrap = new FlowPanel();
        wrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        wrap.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "1fr");
        wrap.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        wrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        wrap.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        return wrap;
    }

    private FlowPanel buildInputWrap(TextBox source) {
        FlowPanel inputWrap = new FlowPanel();
        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "1fr");
        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        inputWrap.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        inputWrap.add(source);
        return inputWrap;
    }

    private FlowPanel buildPreviewWrap(FlowPanel inner) {
        FlowPanel previewWrap = new FlowPanel();
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1.5px solid " + Theme.COLOR_BORDER);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_INPUT);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "28px");
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "100%");
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SIZING, "border-box");
        previewWrap.getElement().getStyle().setProperty(BaseStyle.Key.LINE_HEIGHT, "normal");
        previewWrap.add(inner);
        return previewWrap;
    }
}
