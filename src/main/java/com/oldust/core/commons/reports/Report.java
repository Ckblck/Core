package com.oldust.core.commons.reports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Getter
@RequiredArgsConstructor
public class Report {
    private final String reported;
    private final String reporter;
    private final String reason;
    private final Timestamp date;
}
