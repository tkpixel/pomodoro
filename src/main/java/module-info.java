/**
 * JPMS-Moduldeskriptor für das Pomodoro-Projekt.
 */
module com.signongroup.pomodoro {

    // ── JavaFX ────────────────────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

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

    // ── Jakarta (Annotations) ─────────────────────────────────────────────────
    requires jakarta.inject;
    requires jakarta.annotation;

    // ── Logging ───────────────────────────────────────────────────────────────
    requires org.slf4j;

    // ── java-keyring (Credentials) ────────────────────────────────────────
    requires java.keyring;

    requires java.net.http;
    requires java.prefs;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    // ── Exports (erforderlich damit Micronauts APT die Klassen zur Compile-Zeit sieht) ──
    exports com.signongroup.pomodoro;
    exports com.signongroup.pomodoro.view;
    exports com.signongroup.pomodoro.view.jira;
    exports com.signongroup.pomodoro.viewmodel;
    exports com.signongroup.pomodoro.model;
    exports com.signongroup.pomodoro.model.jira;
    exports com.signongroup.pomodoro.service;


    // ── Reflection-Öffnungen für FXMLLoader ──────────────────────────────────
    opens com.signongroup.pomodoro.view to javafx.fxml, io.micronaut.micronaut_inject;
    opens com.signongroup.pomodoro.view.jira to javafx.fxml, io.micronaut.micronaut_inject;

    // ── Reflection-Öffnungen für Micronaut & JavaFX ───────────────────────────
    opens com.signongroup.pomodoro to javafx.graphics, io.micronaut.micronaut_inject;
    opens com.signongroup.pomodoro.viewmodel to io.micronaut.micronaut_inject;
    opens com.signongroup.pomodoro.service to io.micronaut.micronaut_inject;
    opens com.signongroup.pomodoro.model to io.micronaut.micronaut_inject, com.fasterxml.jackson.databind;
    opens com.signongroup.pomodoro.model.jira to io.micronaut.micronaut_inject, com.fasterxml.jackson.databind;
}





