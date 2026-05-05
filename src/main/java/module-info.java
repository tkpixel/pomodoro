/**
 * JPMS-Moduldeskriptor für das Focus-Projekt.
 */
module com.signongroup.focus {

    // ── JavaFX ────────────────────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    // ── AtlantaFX (Styling) ───────────────────────────────────────────────────
    requires atlantafx.base;

    // ── Ikonli (Icons) ────────────────────────────────────────────────────────
    requires org.kordamp.ikonli.javafx;
    // ── FluentUI-Icons (optional, je nach Bedarf) ─────────────────────────────
    requires org.kordamp.ikonli.fluentui;
    // Core Ikonli runtime requirement for ServiceLoader to find packs
    requires org.kordamp.ikonli.core;

    uses org.kordamp.ikonli.IkonHandler;
    uses org.kordamp.ikonli.IkonProvider;

    // ── Micronaut (DI / IoC) ──────────────────────────────────────────────────
    // Automatic-Module-Namen werden aus dem JAR-Dateinamen abgeleitet:
    // micronaut-core-x.jar → io.micronaut.micronaut_core
    requires io.micronaut.micronaut_core;
    requires io.micronaut.micronaut_inject;
    requires io.micronaut.micronaut_context;
    requires io.micronaut.micronaut_aop;
    requires io.micronaut.micronaut_http;
    requires io.micronaut.micronaut_http_client;
    requires io.micronaut.micronaut_http_client_core;
    requires org.reactivestreams;
    requires io.micronaut.micronaut_jackson_databind;
    requires io.micronaut.micronaut_json_core;

    // ── Jakarta (Annotations) ─────────────────────────────────────────────────
    requires jakarta.inject;
    requires jakarta.annotation;

    // ── Logging ───────────────────────────────────────────────────────────────
    requires org.slf4j;

    // ── java-keyring (Credentials) ────────────────────────────────────────
    requires java.keyring;

    // ── JNA for Native Titlebar Styling ──────────────────────────────────────
    requires com.sun.jna;
    requires com.sun.jna.platform;

    requires java.net.http;
    requires java.prefs;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    // ── Exports (erforderlich damit Micronauts APT die Klassen zur Compile-Zeit sieht) ──
    exports com.signongroup.focus;
    exports com.signongroup.focus.view;
    exports com.signongroup.focus.view.jira;
    exports com.signongroup.focus.viewmodel;
    exports com.signongroup.focus.model;
    exports com.signongroup.focus.model.jira;
    exports com.signongroup.focus.service;
    exports com.signongroup.focus.config;


    // ── Reflection-Öffnungen für FXMLLoader ──────────────────────────────────
    opens com.signongroup.focus.view to javafx.fxml, io.micronaut.micronaut_inject;
    opens com.signongroup.focus.view.jira to javafx.fxml, io.micronaut.micronaut_inject;

    // ── Reflection-Öffnungen für Micronaut & JavaFX ───────────────────────────
    opens com.signongroup.focus to javafx.graphics, io.micronaut.micronaut_inject;
    opens com.signongroup.focus.viewmodel to io.micronaut.micronaut_inject;
    opens com.signongroup.focus.service to io.micronaut.micronaut_inject;
    opens com.signongroup.focus.model to io.micronaut.micronaut_inject, com.fasterxml.jackson.databind;
    opens com.signongroup.focus.model.jira to io.micronaut.micronaut_inject, com.fasterxml.jackson.databind;
    opens com.signongroup.focus.config to io.micronaut.micronaut_inject;
}





