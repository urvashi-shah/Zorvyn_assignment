package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.DashboardSummaryResponse;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final FinancialRecordRepository recordRepository;

    public DashboardService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public DashboardSummaryResponse summary() {
        BigDecimal income = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal expense = recordRepository.sumByType(RecordType.EXPENSE);
        return new DashboardSummaryResponse(income, expense, income.subtract(expense));
    }

    public Map<String, BigDecimal> categoryTotals() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : recordRepository.categoryTotals()) {
            map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }
        return map;
    }

    public Map<String, BigDecimal> monthlyTrends() {
        return recordRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        LinkedHashMap::new,
                        Collectors.mapping(FinancialRecord::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    public List<FinancialRecord> recent() {
        return recordRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
