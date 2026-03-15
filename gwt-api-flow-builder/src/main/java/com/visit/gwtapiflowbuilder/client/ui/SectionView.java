package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

public class SectionView {
    private final String title;
    private final FlowPanel body;
    private final boolean collapsible;

    public SectionView(String title, FlowPanel body, boolean collapsible) {
        this.title = title;
        this.body = body;
        this.collapsible = collapsible;
    }

    public FlowPanel build() {
        FlowPanel section = new FlowPanel();
        section.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        section.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "12px");
        section.getElement().getStyle().setProperty(BaseStyle.Key.OVERFLOW, BaseStyle.Value.HIDDEN);
        section.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 1px 3px rgba(15,23,42,0.08)");

        FlowPanel header = new FlowPanel();
        header.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.FLEX);
        header.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_ITEMS, BaseStyle.Value.CENTER);
        header.getElement().getStyle().setProperty(BaseStyle.Key.JUSTIFY_CONTENT, BaseStyle.Value.SPACE_BETWEEN);
        header.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_SECTION_HDR);
        header.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_IMAGE, Theme.GRADIENT_HEADER);
        header.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, collapsible ? BaseStyle.Value.POINTER : BaseStyle.Value.DEFAULT);

        Label headerTitle = new Label(sectionTitle(title));
        headerTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_87);
        headerTitle.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        headerTitle.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, "#FFFFFF");

        Button toggleButton = UiFactory.ghostButton("Close");
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_68);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "2px 7px");
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        toggleButton.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);

        header.add(headerTitle);
        if (collapsible) header.add(toggleButton);

        FlowPanel bodyWrap = new FlowPanel();
        bodyWrap.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, Theme.PAD_SECTION_BODY);
        bodyWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        bodyWrap.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);
        bodyWrap.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        bodyWrap.add(body);

        if (collapsible) {
            header.addDomHandler(event -> toggleSection(bodyWrap, toggleButton), ClickEvent.getType());
            toggleButton.addClickHandler(event -> {
                event.stopPropagation();
                toggleSection(bodyWrap, toggleButton);
            });
        }

        section.add(header);
        section.add(bodyWrap);
        return section;
    }

    private void toggleSection(FlowPanel bodyWrap, Button toggleButton) {
        boolean open = !"none".equals(bodyWrap.getElement().getStyle().getProperty("display"));
        bodyWrap.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, open ? BaseStyle.Value.NONE : BaseStyle.Value.GRID);
        toggleButton.setText(open ? "Open" : "Close");
    }

    private String sectionTitle(String title) {
        if ("Meta".equalsIgnoreCase(title)) return "▤ Meta";
        if ("Environment".equalsIgnoreCase(title)) return "◍ Environment";
        if ("Global Inputs".equalsIgnoreCase(title)) return "⌗ Inputs";
        if ("Steps".equalsIgnoreCase(title)) return "▸ Steps";
        return title;
    }
}
