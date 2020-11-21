package com.oldust.sync.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerDatabaseKeys {
    RANK("rank"),
    PERSONAL_PERMISSIONS("personal_permissions");

    private final String key;
}
