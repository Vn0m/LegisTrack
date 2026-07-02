package com.legistrack.app.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SavedBillDto(
    UUID id,
    String basePrintNoStr,
    String title,
    OffsetDateTime savedAt,
    String notes,
    List<LabelDto> labels
) {}
