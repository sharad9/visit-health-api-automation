package com.visit.gwtapiflowbuilder.client;

/** GWT-compatible single-string callback (replaces java.util.function.Consumer). */
public interface StringConsumer {
    void accept(String value);
}
