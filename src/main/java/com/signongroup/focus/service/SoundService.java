package com.signongroup.focus.service;

import jakarta.inject.Singleton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

@Singleton
public class SoundService {
    private static final Logger log = LoggerFactory.getLogger(SoundService.class);

    private final BooleanProperty isMuted = new SimpleBooleanProperty(false);

    private AudioClip warningSound;
    private AudioClip alarmSound;

    /**
     * Constructor for SoundService.
     */
    public SoundService() {
        warningSound = loadSound("/audio/warning.wav");
        alarmSound = loadSound("/audio/alarm.wav");
    }

    private AudioClip loadSound(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                return new AudioClip(resource.toExternalForm());
            } else {
                log.warn("Sound file not found: {}", path);
            }
        } catch (IllegalArgumentException | javafx.scene.media.MediaException e) {
            log.warn("Failed to load sound file: {}", path, e);
        }
        return null;
    }

    /**
     * Plays the warning sound.
     */
    public void playWarningSound() {
        playSound(warningSound);
    }

    /**
     * Plays the alarm sound.
     */
    public void playAlarmSound() {
        playSound(alarmSound);
    }

    private void playSound(AudioClip clip) {
        if (clip != null && !isMuted.get()) {
            try {
                clip.play();
            } catch (IllegalArgumentException | javafx.scene.media.MediaException e) {
                log.warn("Error playing sound", e);
            }
        }
    }

    /**
     * Returns the isMuted property.
     * @return BooleanProperty
     */
    public BooleanProperty isMutedProperty() {
        return isMuted;
    }

    /**
     * Returns muted status.
     * @return boolean
     */
    public boolean isMuted() {
        return isMuted.get();
    }

    /**
     * Sets muted status.
     * @param muted true or false
     */
    public void setMuted(boolean muted) {
        this.isMuted.set(muted);
    }
}
