package com.legistrack.app.controller;

import com.legistrack.app.dto.BillRefRequest;
import com.legistrack.app.dto.MessageDto;
import com.legistrack.app.dto.NotesRequest;
import com.legistrack.app.dto.SaveBillRequest;
import com.legistrack.app.dto.SavedBillsResponseDto;
import com.legistrack.app.dto.SavedStatusDto;
import com.legistrack.app.service.SavedBillService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-bills")
public class SavedBillController {

    private final SavedBillService savedBillService;

    public SavedBillController(SavedBillService savedBillService) {
        this.savedBillService = savedBillService;
    }

    @PostMapping("/save")
    public MessageDto saveBill(@AuthenticationPrincipal Jwt jwt, @RequestBody SaveBillRequest request) {
        savedBillService.saveBill(CurrentUser.id(jwt), request.basePrintNoStr(), request.notes());
        return new MessageDto("Bill saved successfully");
    }

    @DeleteMapping("/unsave")
    public MessageDto unsaveBill(@AuthenticationPrincipal Jwt jwt, @RequestBody BillRefRequest request) {
        savedBillService.unsaveBill(CurrentUser.id(jwt), request.basePrintNoStr());
        return new MessageDto("Bill unsaved successfully");
    }

    @GetMapping("/my-bills")
    public SavedBillsResponseDto getMyBills(@AuthenticationPrincipal Jwt jwt) {
        return savedBillService.getUserSavedBills(CurrentUser.id(jwt));
    }

    @GetMapping("/check")
    public SavedStatusDto checkIfSaved(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam("basePrintNoStr") String basePrintNoStr) {
        return savedBillService.getSavedStatus(CurrentUser.id(jwt), basePrintNoStr);
    }

    @PatchMapping("/notes")
    public MessageDto updateNotes(@AuthenticationPrincipal Jwt jwt, @RequestBody NotesRequest request) {
        savedBillService.updateNotes(CurrentUser.id(jwt), request.basePrintNoStr(),
            request.notes() != null ? request.notes() : "");
        return new MessageDto("Notes updated successfully");
    }
}
