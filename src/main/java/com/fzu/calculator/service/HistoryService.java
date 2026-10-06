package com.fzu.calculator.service;

import com.fzu.calculator.dto.HistoryItem;
import com.fzu.calculator.dto.HistoryListResponse;
import com.fzu.calculator.entity.CalculationHistory;
import com.fzu.calculator.exception.ResourceNotFoundException;
import com.fzu.calculator.repository.CalculationHistoryRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read and delete operations on the persisted calculation history.
 */
@Service
public class HistoryService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CalculationHistoryRepository repository;

    public HistoryService(CalculationHistoryRepository repository) {
        this.repository = repository;
    }

    /** Returns the whole history, newest first. */
    @Transactional(readOnly = true)
    public HistoryListResponse findAll() {
        List<CalculationHistory> records =
                repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<HistoryItem> items = records.stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        return new HistoryListResponse(items);
    }

    /**
     * Deletes one history record.
     *
     * @param id identifier of the record to delete
     * @return the identifier that was deleted
     * @throws ResourceNotFoundException when no record with the given id exists
     */
    @Transactional
    public Long delete(Long id) {
        CalculationHistory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "历史记录 " + id + " 不存在"));
        repository.delete(entity);
        return entity.getId();
    }

    private HistoryItem toItem(CalculationHistory entity) {
        return new HistoryItem(entity.getId(), entity.getExpression(),
                new BigDecimal(entity.getResult()),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().format(FORMATTER));
    }
}
