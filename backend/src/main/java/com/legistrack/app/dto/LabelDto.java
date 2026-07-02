package com.legistrack.app.dto;

import com.legistrack.app.model.Label;

import java.util.UUID;

public record LabelDto(UUID id, String label) {
    public static LabelDto from(Label label) {
        return new LabelDto(label.getId(), label.getLabel());
    }
}
