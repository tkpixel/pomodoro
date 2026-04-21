package com.signongroup.pomodoro.service;

import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import jakarta.inject.Singleton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@Singleton
public class ActiveTaskService {
    private final ObjectProperty<TaskCardViewModel> activeTask = new SimpleObjectProperty<>();

    public void setActiveTask(TaskCardViewModel task) {
        this.activeTask.set(task);
    }

    public ObjectProperty<TaskCardViewModel> activeTaskProperty() {
        return activeTask;
    }

    public TaskCardViewModel getActiveTask() {
        return activeTask.get();
    }
}
