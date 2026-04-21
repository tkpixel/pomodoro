package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.BoardLocation;

public record BoardViewModel(Long id, String name, BoardLocation location) {
}
