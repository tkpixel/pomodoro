# Pomodoro Timer

> Eine Pomodoro-Timer-Anwendung, die auf JavaFX basiert und mit JIRA verbunden ist


## 🛠️ Techstack

| Bereich | Technologie | Version |
|---|---|---|
| Sprache | Java | 25 |
| UI-Framework | JavaFX | 25.0.2 |
| UI-Styling | AtlantaFX | 2.1.0 |
| Icons | Ikonli (FluentUI-Pack) | 12.4.0 |
| Dependency Injection | Micronaut (DI/AOP) | 4.10.9 |
| Logging | Logback (SLF4J) | 1.5.32 |
| Build | Maven (Wrapper enthalten) | – |
| Packaging | maven-shade, jlink, jpackage | – |
| Testing | JUnit 5 + Mockito | 6.0.3 / 5.22.0 |
| Code Coverage | JaCoCo | 0.8.14 |

---

## 🏗️ Architektur – MVVM

Das Projekt folgt dem **Model-View-ViewModel (MVVM)**-Pattern, das für JavaFX-Anwendungen besonders
gut geeignet ist, da es JavaFX-Properties zur reaktiven Datenbindung nutzt.

```
┌─────────────────────────────────────────────────────┐
│                     View (.fxml)                     │
│          MainView.fxml  ←→  MainViewController       │
│              (UI-Darstellung & Nutzer-Events)        │
└────────────────────┬────────────────────────────────┘
                     │  bindet / ruft auf
┌────────────────────▼────────────────────────────────┐
│               ViewModel (JavaFX-Properties)          │
│                    MainViewModel                     │
│      (Zustand, Logik, Observable Properties)        │
└────────────────────┬────────────────────────────────┘
                     │  verwendet
┌────────────────────▼────────────────────────────────┐
│                 Model / Services                     │
│          (Domänenobjekte, Business-Logik)            │
└─────────────────────────────────────────────────────┘
```

- **View**: FXML-Dateien definieren das UI, der `Controller` bindet ausschließlich UI-Elemente an das ViewModel – keine Business-Logik.
- **ViewModel**: Hält den UI-Zustand als `Property`-Objekte (z. B. `StringProperty`). Die View bindet sich reaktiv daran.
- **Model/Services**: Reine Domänenlogik ohne JavaFX-Abhängigkeiten – einfach testbar.
- **Micronaut DI**: Controller und ViewModels werden als `@Singleton` von Micronaut verwaltet und per `@Inject` verbunden.

### Paketstruktur

```
com.signongroup.pomodoro
├── model/        # Domänenobjekte / POJOs
├── service/      # Business-Logik, externe Anbindungen
├── view/         # FXML-Controller (nur UI-Binding)
└── viewmodel/    # Observable State (JavaFX Properties)
```

---

## 📋 Logging – Logback

Logging erfolgt über **SLF4J** mit **Logback** als Implementierung.
Die Konfiguration liegt unter `src/main/resources/logback.xml`.

**Highlights:**
- **Konsole**: Immer aktiv, mit kompaktem Zeitformat (`HH:mm:ss.SSS`).
- **Datei**: Rolling-Log unter `~/.templateApp/logs/` – täglich rotiert, max. 10 MB pro Datei, 30 Tage History, 1 GB Cap.
- **Log-Level** ist über die Umgebungsvariable `LOG_LEVEL` konfigurierbar (Standard: `INFO`).
- Eigene Anwendungsklassen (`com.signongroup.pomodoro`) laufen standardmäßig auf `DEBUG`.

```java
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
log.debug("Detail-Info für Entwicklung");
log.info("Anwendung gestartet");
```

---

## 🎨 Corporate Design & AtlantaFX

Das Styling basiert auf **[AtlantaFX](https://github.com/mkpaz/atlantafx)** – einem modernen, CSS-basierten
Theme-Framework für JavaFX. Die Corporate-Design-Farben werden als CSS-Variablen definiert und auf die
AtlantaFX-Framework-Variablen gemappt.

Die zentrale CSS-Datei liegt unter `src/main/resources/css/corporate-design.css`.

### Farbpalette

| Farbe | Hex | Verwendung |
|---|---|---|
| 🟠 Signon Orange | `#F08A16` | Primärfarbe, Buttons, Fokus-Rahmen |
| 🔵 Signon Blau | `#21BBEB` | Info, Hinweise, Tooltips |
| 🟢 Signon Grün | `#30C29E` | Erfolg, Bestätigungen |
| 🟣 Signon Lila | `#5370FB` | Sekundäre Akzente |
| ⬜ Grau | `#828282` | Texte, Rahmen, Hintergründe |

Das Mapping erfolgt direkt auf AtlantaFX-Designtoken (z. B. `-color-accent-emphasis`, `-color-success-emphasis`),
sodass alle AtlantaFX-Komponenten automatisch im Corporate Design erscheinen.

Icons werden über **[Ikonli](https://kordamp.org/ikonli/)** eingebunden (FluentUI-Pack vorinstalliert,
weitere Packs können einfach als Maven-Dependency ergänzt werden).

---

## ✅ Code-Qualität

Das Projekt nutzt mehrere Werkzeuge, um hohe Code-Qualität sicherzustellen:

### Checkstyle
Prüft **Formatierung und Coding-Konventionen** (Einrückung, Imports, Javadoc-Pflichten etc.).
Konfiguration: `checkstyle.xml`

```bash
./mvnw checkstyle:check
```

### PMD
Führt **statische Code-Analyse** durch (Toter Code, unnötige Casts, übermäßige Komplexität etc.).
Konfiguration: `pmd-ruleset.xml`

```bash
./mvnw pmd:check
```

### ErrorProne + NullAway

**Zur Compile-Zeit** werden potenzielle Bugs (ErrorProne) und **`NullPointerException`-Risiken** (NullAway)
erkannt – bevor der Code überhaupt läuft. NullAway ist so konfiguriert, dass es das komplette
`com.signongroup.pomodoro`-Paket auf Null-Sicherheit prüft.

Aktivierung über das Maven-Profil:

```bash
./mvnw clean compile -P errorprone-nullaway
```

> **Hinweis**: Das ErrorProne-Profil kompiliert den Code mit zusätzlichen Prüfungen.  
> Für die normale Entwicklung verwende `./mvnw compile` (ohne Profil).

---

## 🚀 Schnellstart

**Voraussetzungen:** JDK 25, Maven (oder `./mvnw`)

```bash
# Anwendung starten
./mvnw javafx:run

# Tests ausführen
./mvnw test

# Alle Qualitätsprüfungen
./mvnw verify -P errorprone-nullaway

# Ausführbares Fat-JAR bauen
./mvnw package
```
