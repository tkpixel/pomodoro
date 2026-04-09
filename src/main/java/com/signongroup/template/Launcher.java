package com.signongroup.template;

/**
 * Non-JavaFX launcher class required for running the application from a fat JAR.
 * JavaFX's Application.launch() fails when the main class extends Application
 * and the app is started from an uber-jar on the classpath (outside the module path).
 */
public class Launcher {
    public static void main(String[] args) {
        TemplateApplication.main(args);
    }
}

