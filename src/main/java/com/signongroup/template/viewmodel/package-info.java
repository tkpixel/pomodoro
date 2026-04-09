/**
 * ViewModel-Schicht der MVVM-Architektur.
 *
 * <p>Das ViewModel bildet das Herzstück des MVVM-Musters: Es entkoppelt die
 * View vollständig von der Domänenlogik und stellt der View ausschließlich
 * <em>beobachtbare Zustände</em> sowie <em>Aktionen</em> zur Verfügung.
 *
 * <h2>Verantwortlichkeiten</h2>
 * <ul>
 *   <li><strong>Zustandsverwaltung</strong> – Der Präsentationszustand wird über
 *       JavaFX-Properties ({@link javafx.beans.property.Property}) gehalten,
 *       z. B. {@link javafx.beans.property.StringProperty},
 *       {@link javafx.beans.property.BooleanProperty},
 *       {@link javafx.beans.property.ObjectProperty}.
 *       Die View bindet ihre Controls direkt an diese Properties.</li>
 *   <li><strong>Präsentationslogik</strong> – Transformationen, Validierungen und
 *       Formatierungen von Domänendaten für die Anzeige finden hier statt.</li>
 *   <li><strong>Aktionsweiterleitung</strong> – Methoden wie {@code updateGreeting()}
 *       nehmen Benutzerintentionen entgegen und delegieren die eigentliche
 *       Geschäftslogik an Services.</li>
 *   <li><strong>Singleton-Scope</strong> – ViewModels werden von Micronaut als
 *       {@code @Singleton} verwaltet, sodass Zustand über den gesamten
 *       Applikationslebenszyklus erhalten bleibt.</li>
 * </ul>
 *
 * <h2>Erlaubte Abhängigkeiten</h2>
 * <ul>
 *   <li>{@code com.signongroup.template.model} – ViewModels dürfen Domänenobjekte
 *       lesen und in Properties überführen.</li>
 *   <li>{@code com.signongroup.template.service} – Fachlogik wird durch Services
 *       bereitgestellt, die per {@code @Inject} eingebunden werden.</li>
 * </ul>
 *
 * <h2>Verbotene Abhängigkeiten</h2>
 * <ul>
 *   <li>Keinerlei Imports aus {@code com.signongroup.template.view} –
 *       das ViewModel kennt die View <strong>nicht</strong>.</li>
 *   <li>Keine direkten JavaFX-UI-Klassen ({@code Node}, {@code Stage} usw.) –
 *       ausschließlich Properties und Collections aus
 *       {@code javafx.beans} / {@code javafx.collections} sind erlaubt.</li>
 * </ul>
 *
 * <h2>Datenbindung</h2>
 * <p>Die unidirektionale Bindung ({@code view.property.bind(viewModel.property())})
 * stellt sicher, dass Zustandsänderungen automatisch in der UI reflektiert werden,
 * ohne dass das ViewModel die View kennen muss.</p>
 */
package com.signongroup.template.viewmodel;

