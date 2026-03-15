package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;

public class KeyValueRow {
    public final TextBox key;
    public final TextBox value;

    public KeyValueRow(TextBox key, TextBox value) {
        this.key = key;
        this.value = value;
    }

    public FlowPanel container() {
        FlowPanel row = new FlowPanel();
        row.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        row.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "1fr 1fr");
        row.getElement().getStyle().setProperty(BaseStyle.Key.GAP, "6px");
        row.add(key);
        row.add(value);
        return row;
    }
}
