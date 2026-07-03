package com.legistrack.app.controller;

import com.legistrack.app.dto.CreateLabelRequest;
import com.legistrack.app.dto.LabelDto;
import com.legistrack.app.dto.LabelsResponseDto;
import com.legistrack.app.dto.MessageDto;
import com.legistrack.app.service.LabelService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/labels")
public class LabelController {
    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public LabelsResponseDto getAllLabels() {
        return new LabelsResponseDto(labelService.getAllLabels().stream().map(LabelDto::from).toList());
    }

    @PostMapping
    public LabelDto createLabel(@RequestBody CreateLabelRequest request) {
        if (request.label() == null || request.label().isBlank()) {
            throw new IllegalArgumentException("label is required");
        }
        return LabelDto.from(labelService.createLabel(request.label().trim()));
    }

    @DeleteMapping("/{id}")
    public MessageDto deleteLabel(@PathVariable("id") UUID id) {
        labelService.deleteLabel(id);
        return new MessageDto("Label deleted");
    }

    @GetMapping("/bill/{basePrintNoStr}")
    public LabelsResponseDto getLabelsForBill(@PathVariable("basePrintNoStr") String basePrintNoStr) {
        return new LabelsResponseDto(labelService.getLabelsForBill(basePrintNoStr).stream()
            .map(bl -> LabelDto.from(bl.getLabel()))
            .toList());
    }

    @PostMapping("/bill/{basePrintNoStr}/{labelId}")
    public MessageDto addLabelToBill(@PathVariable("basePrintNoStr") String basePrintNoStr,
                                     @PathVariable("labelId") UUID labelId) {
        labelService.addLabelToBill(basePrintNoStr, labelId);
        return new MessageDto("Label added");
    }

    @DeleteMapping("/bill/{basePrintNoStr}/{labelId}")
    public MessageDto removeLabelFromBill(@PathVariable("basePrintNoStr") String basePrintNoStr,
                                          @PathVariable("labelId") UUID labelId) {
        labelService.removeLabelFromBill(basePrintNoStr, labelId);
        return new MessageDto("Label removed");
    }
}
