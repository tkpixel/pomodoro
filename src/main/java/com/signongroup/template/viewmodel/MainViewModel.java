package com.signongroup.template.viewmodel;

import jakarta.inject.Singleton;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel für die Hauptansicht (MVVM-Pattern).
 * Wird von Micronaut als Singleton verwaltet.
 */
@Singleton
public class MainViewModel {

    private final StringProperty greeting = new SimpleStringProperty("Hallo von Micronaut + JavaFX!");

    public StringProperty greetingProperty() {
        return greeting;
    }

    public String getGreeting() {
        return greeting.get();
    }

    public void updateGreeting() {
        greeting.set("Button wurde geklickt! (" + System.currentTimeMillis() + ")");
    }
}
