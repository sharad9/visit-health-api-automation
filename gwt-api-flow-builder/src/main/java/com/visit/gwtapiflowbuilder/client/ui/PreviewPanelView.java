package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

public class PreviewPanelView {
    private final FlowPanel wrapper;
    private final FlowPanel body;
    private final Button toggleButton;
    private final TextArea jsonArea;
    private final Label statusPill;
    private boolean open = true;

    public PreviewPanelView() {
        wrapper = new FlowPanel();
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "42%");
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "360px");
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_SHRINK, "0");
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "100%");
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_DIRECTION, BaseStyle.Value.COLUMN);
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_LEFT, "1px solid " + Theme.COLOR_BORDER);
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_JSON_BG);
        wrapper.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);

        FlowPanel previewHeader = new FlowPanel();
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_SECTION_HDR);
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        previewHeader.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_BOTTOM, "1px solid " + Theme.COLOR_BORDER);

        Label previewTitle = new Label("▧ Preview");
        previewTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        previewTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        previewTitle.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);

        toggleButton = UiFactory.ghostButton("Close");
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        toggleButton.addClickHandler(event -> toggle());

        previewHeader.add(previewTitle);
        previewHeader.add(toggleButton);

        body = new FlowPanel();
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        body.getElement().getStyle().setProperty(BaseStyle.Key.FLEX_DIRECTION, BaseStyle.Value.COLUMN);
        body.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "1");
        body.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "0");

        FlowPanel jsonHeader = new FlowPanel();
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_PREVIEW_HDR);
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        jsonHeader.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_BOTTOM, "1px solid " + Theme.COLOR_BORDER);

        FlowPanel jsonHeaderLeft = new FlowPanel();
        jsonHeaderLeft.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        jsonHeaderLeft.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "2px");
        Label jsonTitle = new Label("▦ JSON Output");
        jsonTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_87);
        jsonTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_700);
        jsonTitle.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        Label jsonSubtitle = new Label("✎ Edit JSON to sync with form");
        jsonSubtitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        jsonSubtitle.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED_DIM);
        jsonHeaderLeft.add(jsonTitle);
        jsonHeaderLeft.add(jsonSubtitle);

        FlowPanel jsonHeaderRight = new FlowPanel();
        jsonHeaderRight.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        jsonHeaderRight.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        jsonHeaderRight.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);

        statusPill = new Label("● Live");
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.INLINE_FLEX);
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "3px");
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, "monospace");
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "2px 5px");
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "12px");
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_STATUS_BG);
        statusPill.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_STATUS_TEXT);

        Button copyButton = new Button("Copy JSON");
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "3px 10px");
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, BaseStyle.Value.NONE);
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PRIMARY);
        copyButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, "#FFFFFF");
        copyButton.addClickHandler(event -> copyJson(copyButton));

        jsonHeaderRight.add(statusPill);
        jsonHeaderRight.add(copyButton);

        jsonHeader.add(jsonHeaderLeft);
        jsonHeader.add(jsonHeaderRight);

        jsonArea = new TextArea();
        jsonArea.setText("{}");
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.FLEX, "1");
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.MIN_HEIGHT, "0");
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_JSON);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.LINE_HEIGHT, "1.7");
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_JSON_BG);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_JSON_TEXT);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, Theme.FONT_MONO);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, BaseStyle.Value.NONE);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.OUTLINE, BaseStyle.Value.NONE);
        jsonArea.getElement().getStyle().setProperty(BaseStyle.Key.RESIZE, BaseStyle.Value.NONE);
        jsonArea.getElement().setAttribute("spellcheck", "false");

        body.add(jsonHeader);
        body.add(jsonArea);

        wrapper.add(previewHeader);
        wrapper.add(body);
    }

    public FlowPanel getPanel() {
        return wrapper;
    }

    public TextArea getJsonArea() {
        return jsonArea;
    }

    public void setJsonText(String text) {
        jsonArea.setText(text);
    }

    public void setJsonStatus(boolean valid) {
        if (valid) {
            statusPill.setText("● Live");
            statusPill.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_STATUS_BG);
            statusPill.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_STATUS_TEXT);
        } else {
            statusPill.setText("⚠ Invalid JSON");
            statusPill.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_STATUS_ERROR_BG);
            statusPill.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_STATUS_ERROR_TEXT);
        }
    }

    private void toggle() {
        open = !open;
        body.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, open ? BaseStyle.Value.FLEX : BaseStyle.Value.NONE);
        toggleButton.setText(open ? "Close" : "Open");
    }

    private void copyJson(Button button) {
        copyToClipboard(jsonArea.getText());
        button.setText("✔ Copied");
        Timer timer = new Timer() {
            @Override
            public void run() {
                button.setText("Copy JSON");
            }
        };
        timer.schedule(1500);
    }

    private native void copyToClipboard(String text) /*-{
        if ($wnd && $wnd.navigator && $wnd.navigator.clipboard) {
            $wnd.navigator.clipboard.writeText(text);
        }
    }-*/;
}
