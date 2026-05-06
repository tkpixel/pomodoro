package com.signongroup.focus.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserPreferencesService {

    private final SecretManager secretManager;

    @Inject
    public UserPreferencesService(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    public void saveLastBoardId(String boardId) {
        secretManager.savePlaintext("jira_selected_board_id", boardId);
    }

    public String getLastBoardId() {
        return secretManager.getPlaintext("jira_selected_board_id");
    }
}
