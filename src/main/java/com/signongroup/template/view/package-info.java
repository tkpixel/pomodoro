/**
 * View-Schicht der MVVM-Architektur.
 *
 * <p>Dieses Paket enthält alle Klassen und FXML-Ressourcen, die unmittelbar für die
 * Darstellung der Benutzeroberfläche verantwortlich sind. In der MVVM-Terminologie
 * entspricht die <em>View</em> dem passiven Präsentationsrahmen:
 *
 * <h2>Verantwortlichkeiten</h2>
 * <ul>
 *   <li><strong>FXML-Deklaration</strong> – Die Struktur der UI (Layout, Controls, Styles)
 *       wird ausschließlich in {@code *.fxml}-Dateien beschrieben, die vom JavaFX
 *       {@link javafx.fxml.FXMLLoader} geladen werden.</li>
 *   <li><strong>Controller</strong> – Jede FXML-Datei besitzt einen zugehörigen Controller
 *       (z. B. {@link com.signongroup.template.view.MainViewController}), der:
 *       <ol>
 *         <li>Via {@code @FXML} auf UI-Elemente zugreift.</li>
 *         <li>Properties der View an Properties des ViewModels bindet
 *             ({@link javafx.beans.property.Property#bind(javafx.beans.value.ObservableValue)}).</li>
 *         <li>Benutzeraktionen (Button-Klicks, Eingaben) als Methodenaufrufe an das ViewModel
 *             weiterleitet – <em>ohne eigene Geschäftslogik</em>.</li>
 *       </ol>
 *   </li>
 *   <li><strong>Dependency Injection</strong> – Controller werden von Micronaut als
 *       {@code @Singleton} verwaltet. Das ViewModel wird per Konstruktor-Injektion
 *       ({@code @Inject}) eingebunden; der {@code FXMLLoader} nutzt
 *       {@code context::getBean} als Controller-Factory.</li>
 * </ul>
 *
 * <h2>Erlaubte Abhängigkeiten</h2>
 * <ul>
 *   <li>{@code com.signongroup.template.viewmodel} – Controller dürfen ViewModels kennen
 *       und an deren Properties binden.</li>
 * </ul>
 *
 * <h2>Verbotene Abhängigkeiten</h2>
 * <ul>
 *   <li>Direkter Zugriff auf {@code model}- oder {@code service}-Pakete ist
 *       <strong>nicht erlaubt</strong>; diese Schichten werden ausschließlich durch
 *       das ViewModel vermittelt.</li>
 * </ul>
 */
package com.signongroup.template.view;

