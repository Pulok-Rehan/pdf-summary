package com.bracepl.pdf_summary.repository;

import com.bracepl.pdf_summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
}
