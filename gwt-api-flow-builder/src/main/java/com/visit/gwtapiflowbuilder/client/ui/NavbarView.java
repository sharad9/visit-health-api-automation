package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

public class NavbarView {
    public Button saveButton;
    public Button runButton;

    public FlowPanel build(ClickHandler onToggle, String toggleLabel, ClickHandler onSave, ClickHandler onRun, ListBox flowSelect) {
        FlowPanel navbar = new FlowPanel();
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_NAVBAR);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, Theme.HEIGHT_NAVBAR);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_BOTTOM, "1px solid " + Theme.COLOR_BORDER);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 2px 20px rgba(15,23,42,0.12)");
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.POSITION, BaseStyle.Value.RELATIVE);
        navbar.getElement().getStyle().setProperty(BaseStyle.Key.Z_INDEX, "5");

        FlowPanel leftPanel = new FlowPanel();
        leftPanel.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        leftPanel.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        leftPanel.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        Label appIcon = new Label("▣ API");
        appIcon.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_140);
        appIcon.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);

        FlowPanel titleWrap = new FlowPanel();
        titleWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        Label title = new Label("Visit Health Api Flow Builder");
        title.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_95);
        title.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_700);
        title.getElement().getStyle().setProperty(BaseStyle.Key.LETTER_SPACING, Theme.LETTER_SPACING_TIGHT);
        title.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        Label subtitle = new Label("⚙ Design - Preview - Export");
        subtitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        subtitle.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
        titleWrap.add(title);
        titleWrap.add(subtitle);

        leftPanel.add(appIcon);
        leftPanel.add(titleWrap);

        FlowPanel rightPanel = new FlowPanel();
        rightPanel.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        rightPanel.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        rightPanel.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        runButton = UiFactory.outlineButton("▶ Run Collection");
        runButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        runButton.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        runButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEMPLATE);
        runButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_COLOR, Theme.COLOR_TEMPLATE);
        if (onRun != null) runButton.addClickHandler(onRun);

        saveButton = UiFactory.outlineButton("Save");
        saveButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        saveButton.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        if (onSave != null) saveButton.addClickHandler(onSave);

        Button toggleButton = UiFactory.ghostButton(toggleLabel);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        if (onToggle != null) toggleButton.addClickHandler(onToggle);

        if (flowSelect != null) {
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "3px 8px");
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
            flowSelect.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        }

        Label productTag = new Label("◆ API Flow JSON Generator");
        productTag.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        productTag.getElement().getStyle().setProperty(BaseStyle.Key.FONT_FAMILY, "monospace");
        productTag.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_MUTED);
        rightPanel.add(runButton);
        rightPanel.add(saveButton);
        rightPanel.add(toggleButton);
        if (flowSelect != null) rightPanel.add(flowSelect);
        rightPanel.add(productTag);

        navbar.add(leftPanel);
        navbar.add(rightPanel);
        return navbar;
    }
}
