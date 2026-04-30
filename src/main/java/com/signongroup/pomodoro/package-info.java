/**
 * Wurzelpaket der Pomodoro-Applikation.
 *
 * <p>Dieses Paket bildet den Einstiegspunkt der JavaFX-Anwendung und enthält die zentralen
 * Infrastrukturklassen, die den Applikationslebenszyklus steuern:
 *
 * <ul>
 *   <li>{@link com.signongroup.pomodoro.Launcher} – reiner JavaFX-Startpunkt, der die {@link
 *       javafx.application.Application#launch(Class, String[]) launch}-Methode aufruft, damit das
 *       Modul-System korrekt funktioniert.
 *   <li>{@link com.signongroup.pomodoro.PomodoroApplication} – erweitert {@link
 *       javafx.application.Application} und übernimmt drei Aufgaben im MVVM-Kontext:
 *       <ol>
 *         <li><strong>Composition Root</strong>: Startet den Micronaut-{@code ApplicationContext}
 *             und macht ihn als DI-Container für alle Schichten verfügbar.
 *         <li><strong>Bootstrap der View-Schicht</strong>: Lädt das initiale FXML-Dokument und
 *             delegiert die Controller-Erzeugung an den DI-Container ({@code
 *             loader.setControllerFactory(context::getBean)}), sodass Micronaut die View-Controller
 *             mit ihren ViewModels verdrahten kann.
 *         <li><strong>Lifecycle-Management</strong>: Schließt den {@code ApplicationContext}
 *             sauber, wenn die Anwendung beendet wird.
 *       </ol>
 * </ul>
 *
 * <h2>MVVM-Schichtübersicht</h2>
 *
 * <pre>
 * ┌──────────────────────────────────────────────────────────────┐
 * │  com.signongroup.pomodoro          (Composition Root / App)  │
 * │  com.signongroup.pomodoro.view     (View  – FXML + Controller)│
 * │  com.signongroup.pomodoro.viewmodel(ViewModel – Zustand/Logik)│
 * │  com.signongroup.pomodoro.model    (Model  – Domänenobjekte)  │
 * │  com.signongroup.pomodoro.service  (Service – Geschäftslogik) │
 * └──────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>Abhängigkeitsregel: Jede Schicht darf nur von innen nach außen zeigen – View → ViewModel →
 * Model/Service. Das Wurzelpaket kennt alle Schichten, jedoch ausschließlich zur Initialisierung.
 */
package com.signongroup.pomodoro;
