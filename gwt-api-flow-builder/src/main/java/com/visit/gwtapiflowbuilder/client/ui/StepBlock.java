package com.visit.gwtapiflowbuilder.client.ui;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.ArrayList;
import java.util.List;

public class StepBlock {
    public final List<KeyValueRow> headers = new ArrayList<>();
    public final List<KeyValueRow> requestVariables = new ArrayList<>();
    public final List<KeyValueRow> body = new ArrayList<>();
    public final List<KeyValueRow> extractBody = new ArrayList<>();
    public final List<KeyValueRow> extractHeaders = new ArrayList<>();
    public final List<CheckRow> checks = new ArrayList<>();
    public TextBox stepId;
    public ListBox method;
    public TextBox url;
    public TextBox timeoutMs;
    public TextBox retryCount;
    public TextBox retryDelay;
    public Label runResponse;
}
