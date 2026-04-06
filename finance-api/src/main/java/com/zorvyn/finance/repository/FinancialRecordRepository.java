package com.zorvyn.finance.repository;

import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {
    List<FinancialRecord> findByDateBetween(LocalDate from, LocalDate to);
    List<FinancialRecord> findByCategoryIgnoreCase(String category);
    List<FinancialRecord> findByType(RecordType type);

    @Query("select coalesce(sum(r.amount), 0) from FinancialRecord r where r.type = :type")
    BigDecimal sumByType(RecordType type);

    @Query("select r.category, coalesce(sum(r.amount), 0) from FinancialRecord r group by r.category")
    List<Object[]> categoryTotals();

    List<FinancialRecord> findTop5ByOrderByCreatedAtDesc();
}
