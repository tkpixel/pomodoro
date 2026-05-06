package com.signongroup.focus.viewmodel;

import com.signongroup.focus.model.jira.BoardLocation;

public record BoardViewModel(Long id, String name, String type, BoardLocation location) {
}
