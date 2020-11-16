package com.oldust.sync.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerDatabaseKeys {
    RANK("rank");

    private final String key;
}
