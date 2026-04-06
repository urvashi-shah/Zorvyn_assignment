package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.CreateRecordRequest;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecordService {
    private final FinancialRecordRepository recordRepository;

    public RecordService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public FinancialRecord create(CreateRecordRequest request) {
        FinancialRecord record = new FinancialRecord();
        map(record, request);
        return recordRepository.save(record);
    }

    public FinancialRecord update(Long id, CreateRecordRequest request) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        map(record, request);
        return recordRepository.save(record);
    }

    public List<FinancialRecord> list(LocalDate from, LocalDate to, String category, RecordType type) {
        if (from != null && to != null) {
            return recordRepository.findByDateBetween(from, to);
        }
        if (category != null && !category.isBlank()) {
            return recordRepository.findByCategoryIgnoreCase(category);
        }
        if (type != null) {
            return recordRepository.findByType(type);
        }
        return recordRepository.findAll();
    }

    public FinancialRecord get(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
    }

    public void delete(Long id) {
        FinancialRecord record = get(id);
        recordRepository.delete(record);
    }

    private void map(FinancialRecord record, CreateRecordRequest request) {
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
    }
}
