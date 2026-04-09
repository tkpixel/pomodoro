/**
 * Model-Schicht der MVVM-Architektur.
 *
 * <p>Dieses Paket enthält die reinen Domänenobjekte (Entitäten, Wertobjekte, DTOs),
 * die den fachlichen Kern der Anwendung abbilden. Das Model ist vollständig
 * unabhängig von JavaFX und von jeglicher Präsentationslogik.
 *
 * <h2>Verantwortlichkeiten</h2>
 * <ul>
 *   <li><strong>Domänenrepräsentation</strong> – Klassen in diesem Paket modellieren
 *       fachliche Konzepte (z. B. Entitäten, Records, Enums) ausschließlich in
 *       reinem Java ohne Framework-Abhängigkeiten.</li>
 *   <li><strong>Datenstruktur</strong> – POJOs, Records oder Value-Objects, die zwischen
 *       Service- und ViewModel-Schicht ausgetauscht werden.</li>
 *   <li><strong>Validierungsregeln</strong> – Grundlegende fachliche Invarianten
 *       (z. B. über Konstruktoren oder Factory-Methoden) können hier verankert sein.</li>
 * </ul>
 *
 * <h2>Erlaubte Abhängigkeiten</h2>
 * <ul>
 *   <li>Keine externen Framework-Abhängigkeiten – das Model soll portabel und
 *       testbar bleiben.</li>
 *   <li>Standard-Java-Bibliotheken ({@code java.util}, {@code java.time} usw.)
 *       sind erlaubt.</li>
 * </ul>
 *
 * <h2>Verbotene Abhängigkeiten</h2>
 * <ul>
 *   <li>Keine Importe aus {@code com.signongroup.template.view},
 *       {@code com.signongroup.template.viewmodel} oder
 *       {@code com.signongroup.template.service}.</li>
 *   <li>Keine JavaFX-Properties oder UI-Klassen – das Model ist UI-agnostisch.</li>
 * </ul>
 *
 * <h2>Rolle im MVVM-Datenfluss</h2>
 * <pre>
 * Service  →  Model-Objekte  →  ViewModel  →  (Properties)  →  View
 * </pre>
 * <p>Services produzieren bzw. konsumieren Model-Objekte; das ViewModel konvertiert
 * sie bei Bedarf in beobachtbare JavaFX-Properties zur Anzeige.</p>
 */
package com.signongroup.template.model;

