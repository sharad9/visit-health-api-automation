package com.visit.gwtapiflowbuilder.client.service;

import com.google.gwt.json.client.JSONValue;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;

public final class JsonPrettyPrinter {
    private JsonPrettyPrinter() {
    }

    public static String prettyPrint(JSONValue value) {
        if (value == null) return "{}";
        return prettyPrintText(value.toString());
    }

    public static String prettyPrintText(String text) {
        if (text == null) return "{}";
        try {
            return stringify(parse(text), null, 2);
        } catch (Throwable ignore) {
            return text;
        }
    }

    @JsMethod(namespace = JsPackage.GLOBAL, name = "JSON.parse")
    private static native Object parse(String text);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "JSON.stringify")
    private static native String stringify(Object value, Object replacer, int space);
}
