package com.signongroup.focus.model;

public enum FocusProfile {

    POMODORO("Pomodoro", 25, 5, 15, 4),
    RULE_52_17("52/17",   52, 17, 17, 4),
    DEEP_WORK("90-Min",   90, 20, 30, 4),
    CUSTOM("Custom",       0,  0,  0, 0);

    private final String displayName;
    private final int focusMinutes;
    private final int shortBreakMinutes;
    private final int longBreakMinutes;
    private final int intervals;

    FocusProfile(String displayName, int focusMinutes, int shortBreakMinutes,
                 int longBreakMinutes, int intervals) {
        this.displayName       = displayName;
        this.focusMinutes      = focusMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakMinutes  = longBreakMinutes;
        this.intervals         = intervals;
    }

    /**
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the focus minutes
     */
    public int getFocusMinutes() {
        return focusMinutes;
    }

    /**
     * @return the short break minutes
     */
    public int getShortBreakMinutes() {
        return shortBreakMinutes;
    }

    /**
     * @return the long break minutes
     */
    public int getLongBreakMinutes() {
        return longBreakMinutes;
    }

    /**
     * @return the intervals
     */
    public int getIntervals() {
        return intervals;
    }
}
