/**
 * Service-Schicht der MVVM-Architektur.
 *
 * <p>Dieses Paket enthält die Geschäftslogik der Anwendung. Services sind die einzige Schicht, die
 * fachliche Operationen ausführt, externe Systeme (Datenbanken, APIs, Dateisystem) anspricht und
 * den Zustand von Domänenobjekten manipuliert.
 *
 * <h2>Verantwortlichkeiten</h2>
 *
 * <ul>
 *   <li><strong>Geschäftslogik</strong> – Komplexe fachliche Abläufe, die über einfache
 *       CRUD-Operationen hinausgehen, werden hier kapselt.
 *   <li><strong>Datenzugriff / Integration</strong> – Kommunikation mit Datenquellen (Repositories,
 *       REST-Clients, Dateisystem) erfolgt ausschließlich in dieser Schicht.
 *   <li><strong>Transaktions- und Fehlerbehandlung</strong> – Services sind verantwortlich für
 *       konsistente Zustandsübergänge und das Weiterleiten von Fehlern an das ViewModel.
 *   <li><strong>Singleton-Scope</strong> – Services werden von Micronaut als {@code @Singleton}
 *       verwaltet und per {@code @Inject} in ViewModels eingebunden.
 * </ul>
 *
 * <h2>Erlaubte Abhängigkeiten</h2>
 *
 * <ul>
 *   <li>{@code com.signongroup.template.model} – Services operieren auf Domänenobjekten.
 *   <li>Externe Bibliotheken (Datenbankzugriff, HTTP-Clients, usw.).
 * </ul>
 *
 * <h2>Verbotene Abhängigkeiten</h2>
 *
 * <ul>
 *   <li>Keine Imports aus {@code com.signongroup.template.view} oder {@code
 *       com.signongroup.template.viewmodel} – Services sind vollständig UI-agnostisch.
 *   <li>Keine JavaFX-Properties oder UI-Klassen – Rückgabewerte sind stets Plain-Java-Typen oder
 *       Model-Objekte.
 * </ul>
 *
 * <h2>Kommunikation mit dem ViewModel</h2>
 *
 * <p>Das ViewModel ruft Services synchron oder asynchron auf und überführt die erhaltenen
 * Model-Objekte anschließend selbst in JavaFX-Properties. Damit bleibt der Service vollständig
 * entkoppelt vom JavaFX-Threading-Modell.
 */
package com.signongroup.pomodoro.service;
