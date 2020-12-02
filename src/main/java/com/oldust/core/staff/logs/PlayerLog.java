package com.oldust.core.staff.logs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PlayerLog {
    private final int id;
    private final UUID uuid;
    private final String ip;
    private final Date join;
    private final Date exit;
}
