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

    // ── Exports (erforderlich damit Micronauts APT die Klassen zur Compile-Zeit sieht) ──
    exports com.signongroup.pomodoro;
    exports com.signongroup.pomodoro.view;
    exports com.signongroup.pomodoro.viewmodel;
//    exports com.signongroup.pomodoro.model;
//    exports com.signongroup.pomodoro.service;


    // ── Reflection-Öffnungen für FXMLLoader ──────────────────────────────────
    opens com.signongroup.pomodoro.view to javafx.fxml, io.micronaut.micronaut_inject;

    // ── Reflection-Öffnungen für Micronaut & JavaFX ───────────────────────────
    opens com.signongroup.pomodoro to javafx.graphics, io.micronaut.micronaut_inject;
    opens com.signongroup.pomodoro.viewmodel to io.micronaut.micronaut_inject;
}





