package com.legistrack.app.dto;

import java.util.List;

public record SearchResponseDto(List<BillSummaryDto> apiResults, List<BillSummaryDto> localResults) {}
