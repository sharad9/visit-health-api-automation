package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.visit.gwtapiflowbuilder.client.style.BaseStyle;
import com.visit.gwtapiflowbuilder.client.theme.Theme;

public final class UiFactory {
    private UiFactory() {
    }

    public static FlowPanel row(String template) {
        FlowPanel row = new FlowPanel();
        row.getElement().getStyle().setProperty(BaseStyle.Key.DISPLAY, BaseStyle.Value.GRID);
        row.getElement().getStyle().setProperty(BaseStyle.Key.GRID_TEMPLATE_COLUMNS, template);
        row.getElement().getStyle().setProperty(BaseStyle.Key.GAP, Theme.GAP_8);
        return row;
    }

    public static FlowPanel field(String labelText, Widget input) {
        FlowPanel field = new FlowPanel();
        field.getElement().getStyle().setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        Label label = new Label(labelText);
        label.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_72);
        label.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        label.getElement().getStyle().setProperty(BaseStyle.Key.LETTER_SPACING, Theme.LETTER_SPACING_WIDE);
        label.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_PRIMARY);
        label.getElement().getStyle().setProperty(BaseStyle.Key.TEXT_TRANSFORM, "uppercase");
        label.getElement().getStyle().setProperty(BaseStyle.Key.MARGIN_BOTTOM, "6px");
        field.add(label);
        field.add(input);
        return field;
    }

    public static Label smallLabel(String text) {
        Label label = new Label(text);
        label.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_72);
        label.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_700);
        label.getElement().getStyle().setProperty(BaseStyle.Key.LETTER_SPACING, Theme.LETTER_SPACING_WIDE);
        label.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_PRIMARY);
        label.getElement().getStyle().setProperty(BaseStyle.Key.TEXT_TRANSFORM, "uppercase");
        return label;
    }

    public static Button outlineButton(String text) {
        Button button = new Button(text);
        button.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1.5px solid " + Theme.COLOR_PRIMARY);
        button.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_PRIMARY);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PRIMARY_LIGHT);
        button.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "4px 8px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        return button;
    }

    public static Button ghostButton(String text) {
        Button button = new Button(text);
        button.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        button.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "4px 7px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_600);
        button.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        return button;
    }

    public static Button removeButton(String text) {
        Button button = new Button(text);
        button.getElement().getStyle().setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.POINTER);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER, "1px solid " + Theme.COLOR_BORDER);
        button.getElement().getStyle().setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        button.getElement().getStyle().setProperty(BaseStyle.Key.PADDING, "4px 6px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_78);
        button.getElement().getStyle().setProperty(BaseStyle.Key.FONT_WEIGHT, Theme.FONT_WEIGHT_700);
        button.getElement().getStyle().setProperty(BaseStyle.Key.COLOR, Theme.COLOR_DANGER);
        return button;
    }

    public static void styleInput(Widget widget) {
        Style style = widget.getElement().getStyle();
        style.setProperty(BaseStyle.Key.WIDTH, "100%");
        style.setProperty(BaseStyle.Key.MIN_WIDTH, "0");
        style.setProperty(BaseStyle.Key.BOX_SIZING, "border-box");
        style.setProperty(BaseStyle.Key.PADDING, Theme.PAD_INPUT);
        style.setProperty(BaseStyle.Key.BORDER, "1.5px solid " + Theme.COLOR_BORDER);
        style.setProperty(BaseStyle.Key.BORDER_RADIUS, "8px");
        style.setProperty(BaseStyle.Key.FONT_SIZE, Theme.FONT_SIZE_82);
        style.setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        style.setProperty(BaseStyle.Key.COLOR, Theme.COLOR_TEXT);
        style.setProperty(BaseStyle.Key.OUTLINE, BaseStyle.Value.NONE);

        if (widget instanceof FocusWidget) {
            FocusWidget focusWidget = (FocusWidget) widget;
            focusWidget.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    widget.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_COLOR, Theme.COLOR_PRIMARY);
                    widget.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, "0 0 0 3px rgba(255,108,55,0.28)");
                }
            });
            focusWidget.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    widget.getElement().getStyle().setProperty(BaseStyle.Key.BORDER_COLOR, Theme.COLOR_BORDER);
                    widget.getElement().getStyle().setProperty(BaseStyle.Key.BOX_SHADOW, BaseStyle.Value.NONE);
                }
            });
        }
    }

    public static void enablePreviewToggle(TextBox box) {
        if (box == null) {
            return;
        }
        box.setReadOnly(true);
        applyPreviewStyle(box);
        box.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                box.setReadOnly(false);
                applyEditStyle(box);
                box.setFocus(true);
                box.selectAll();
            }
        }, DoubleClickEvent.getType());
        box.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == 13) {
                    box.setReadOnly(true);
                    applyPreviewStyle(box);
                }
            }
        });
        box.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                box.setReadOnly(true);
                applyPreviewStyle(box);
            }
        });
    }

    private static void applyPreviewStyle(TextBox box) {
        Style style = box.getElement().getStyle();
        style.setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_SECTION);
        style.setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.DEFAULT);
    }

    private static void applyEditStyle(TextBox box) {
        Style style = box.getElement().getStyle();
        style.setProperty(BaseStyle.Key.BACKGROUND_COLOR, Theme.COLOR_PANEL);
        style.setProperty(BaseStyle.Key.CURSOR, BaseStyle.Value.TEXT);
    }
}
