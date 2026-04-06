package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.CreateRecordRequest;
import com.zorvyn.finance.dto.RecordResponse;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public List<RecordResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) RecordType type
    ) {
        return recordService.list(from, to, category, type).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public RecordResponse get(@PathVariable Long id) {
        return toResponse(recordService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RecordResponse create(@Valid @RequestBody CreateRecordRequest request) {
        return toResponse(recordService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RecordResponse update(@PathVariable Long id, @Valid @RequestBody CreateRecordRequest request) {
        return toResponse(recordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }

    private RecordResponse toResponse(FinancialRecord r) {
        RecordResponse response = new RecordResponse();
        response.setId(r.getId());
        response.setAmount(r.getAmount());
        response.setType(r.getType());
        response.setCategory(r.getCategory());
        response.setDate(r.getDate());
        response.setNotes(r.getNotes());
        return response;
    }
}
