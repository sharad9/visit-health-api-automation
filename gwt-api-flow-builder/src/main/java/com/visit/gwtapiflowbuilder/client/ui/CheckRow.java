package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;

public class CheckRow {
    public final ListBox source;
    public final TextBox path;
    public final TextBox equals;
    public final CheckBox exists;
    private final Widget pathWidget;
    private final Widget equalsWidget;

    public CheckRow(ListBox source, TextBox path, Widget pathWidget, TextBox equals, Widget equalsWidget, CheckBox exists) {
        this.source = source;
        this.path = path;
        this.equals = equals;
        this.exists = exists;
        this.pathWidget = pathWidget == null ? path : pathWidget;
        this.equalsWidget = equalsWidget == null ? equals : equalsWidget;
    }

    public FlowPanel container() {
        FlowPanel row = new FlowPanel();
        row.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        row.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "120px 1fr 1fr 100px");
        row.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "6px");
        row.add(source);
        row.add(pathWidget);
        row.add(equalsWidget);
        row.add(exists);
        return row;
    }
}
