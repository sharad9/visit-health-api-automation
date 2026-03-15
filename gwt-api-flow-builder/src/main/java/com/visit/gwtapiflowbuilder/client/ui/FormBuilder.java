package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.visit.gwtapiflowbuilder.client.AppState;
import com.visit.gwtapiflowbuilder.client.model.KeyValuePair;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for form input elements and key-value row sections.
 * Calls state.onUpdate.run() on any value change.
 */
public final class FormBuilder {

    private final AppState state;
    private final TokenRenderer tokenRenderer;

    public FormBuilder(AppState state, TokenRenderer tokenRenderer) {
        this.state = state;
        this.tokenRenderer = tokenRenderer;
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    public FlowPanel row() {
        return UiFactory.row("repeat(auto-fit, minmax(180px, 1fr))");
    }

    public FlowPanel row(String template) {
        return UiFactory.row(template);
    }

    // -------------------------------------------------------------------------
    // Input factories
    // -------------------------------------------------------------------------

    public TextBox textBox(String value, String placeholder) {
        TextBox box = new TextBox();
        box.setValue(value);
        box.getElement().setAttribute("placeholder", placeholder);
        UiFactory.styleInput(box);
        bindUpdate(box);
        return box;
    }

    public TextBox textBoxNoPreview(String value, String placeholder) {
        TextBox box = new TextBox();
        box.setValue(value);
        box.getElement().setAttribute("placeholder", placeholder);
        UiFactory.styleInput(box);
        bindUpdate(box);
        return box;
    }

    public TextBox numberBox(String value) {
        TextBox box = new TextBox();
        box.setValue(value);
        box.getElement().setAttribute("type", "number");
        UiFactory.styleInput(box);
        bindUpdate(box);
        return box;
    }

    public ListBox select(String... options) {
        ListBox list = new ListBox();
        for (String option : options) {
            list.addItem(option);
        }
        UiFactory.styleInput(list);
        bindUpdate(list);
        return list;
    }

    public ListBox selectEnvironments() {
        ListBox list = new ListBox();
        if (state.environments.isEmpty()) {
            list.addItem("STAGE");
            list.addItem("UAT");
            list.addItem("PROD");
        } else {
            for (com.visit.gwtapiflowbuilder.client.model.EnvironmentItem env : state.environments) {
                list.addItem(env.name);
            }
        }
        UiFactory.styleInput(list);
        list.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "fit-content");
        list.getElement().getStyle().setProperty(BaseStyle.Key.MAX_WIDTH, "180px");
        bindUpdate(list);
        if (state.activeEnvIndex >= 0 && state.activeEnvIndex < list.getItemCount()) {
            list.setSelectedIndex(state.activeEnvIndex);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Key-value section
    // -------------------------------------------------------------------------

    public FlowPanel keyValueSection(String title, List<KeyValueRow> rows, List<KeyValuePair> initialPairs) {
        FlowPanel section = new FlowPanel();
        section.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        section.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);

        Label label = UiFactory.smallLabel(title);
        section.add(label);

        FlowPanel list = new FlowPanel();
        list.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        list.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        section.add(list);

        Button add = UiFactory.ghostButton("＋ Add");
        add.addClickHandler(event -> {
            addKeyValueRow(list, rows, "", "", "key", "value");
            if (state.onUpdate != null) state.onUpdate.run();
        });

        populateKeyValueRows(list, rows, initialPairs, "key", "value");

        section.add(add);
        return section;
    }

    public void addKeyValueRow(FlowPanel list, List<KeyValueRow> rows,
                               String key, String value,
                               String keyPlaceholder, String valuePlaceholder) {
        KeyValueRow row = new KeyValueRow(textBox(key, keyPlaceholder), textBox(value, valuePlaceholder));
        rows.add(row);
        list.add(buildKeyValueRowContainer(row, rows, list));
    }

    public void populateKeyValueRows(FlowPanel list, List<KeyValueRow> rows,
                                     List<KeyValuePair> pairs,
                                     String keyPlaceholder, String valuePlaceholder) {
        if (pairs == null || pairs.isEmpty()) {
            addKeyValueRow(list, rows, "", "", keyPlaceholder, valuePlaceholder);
            return;
        }
        for (KeyValuePair pair : pairs) {
            addKeyValueRow(list, rows, pair.key, pair.value, keyPlaceholder, valuePlaceholder);
        }
    }

    public FlowPanel buildKeyValueRowContainer(KeyValueRow row, List<KeyValueRow> rows, FlowPanel list) {
        FlowPanel container = new FlowPanel();
        container.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        container.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, "minmax(0, 1fr) minmax(0, 1fr) 32px");
        container.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_6);
        container.add(tokenRenderer.previewToggle(row.key));
        container.add(tokenRenderer.previewToggle(row.value));

        Button remove = UiFactory.removeButton("✖");
        remove.getElement().getStyle().setProperty(BaseStyle.Key.WIDTH, "32px");
        remove.getElement().getStyle().setProperty(BaseStyle.Key.HEIGHT, "28px");
        remove.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "0");
        remove.getElement().getStyle().setProperty(BaseStyle.Key.ALIGN_SELF, BaseStyle.Value.CENTER);
        remove.addClickHandler(event -> {
            rows.remove(row);
            list.remove(container);
            if (state.onUpdate != null) state.onUpdate.run();
        });
        container.add(remove);
        return container;
    }

    // -------------------------------------------------------------------------
    // Bind update helpers
    // -------------------------------------------------------------------------

    public void bindUpdate(TextBox box) {
        box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (state.onUpdate != null) state.onUpdate.run();
            }
        });
    }

    public void bindUpdate(ListBox listBox) {
        listBox.addChangeHandler((ChangeHandler) event -> {
            if (state.onUpdate != null) state.onUpdate.run();
        });
    }

    public void bindUpdate(CheckBox checkBox) {
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (state.onUpdate != null) state.onUpdate.run();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    public int indexOf(ListBox listBox, String value) {
        for (int i = 0; i < listBox.getItemCount(); i++) {
            if (listBox.getItemText(i).equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    public String selectedText(ListBox listBox) {
        if (listBox == null) {
            return "";
        }
        int idx = listBox.getSelectedIndex();
        if (idx < 0 || idx >= listBox.getItemCount()) {
            return "";
        }
        return listBox.getItemText(idx);
    }

    public List<KeyValuePair> toPairs(List<KeyValueRow> rows) {
        List<KeyValuePair> pairs = new ArrayList<>();
        for (KeyValueRow row : rows) {
            pairs.add(new KeyValuePair(row.key.getValue(), row.value.getValue()));
        }
        return pairs;
    }

    public String emptyIfNull(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
