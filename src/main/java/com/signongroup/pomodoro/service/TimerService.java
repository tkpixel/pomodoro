package com.signongroup.pomodoro.service;

import jakarta.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

@Singleton
public class TimerService {

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask;
    private Runnable tickCallback;

    public TimerService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TimerService-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void setTickCallback(Runnable tickCallback) {
        this.tickCallback = tickCallback;
    }

    public void start() {
        if (currentTask == null || currentTask.isCancelled()) {
            currentTask = scheduler.scheduleAtFixedRate(() -> {
                if (tickCallback != null) {
                    tickCallback.run();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    public void pause() {
        if (currentTask != null) {
            currentTask.cancel(false);
            currentTask = null;
        }
    }
}
