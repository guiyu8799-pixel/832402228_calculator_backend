package com.fzu.calculator.dto;

import java.util.List;

/**
 * Response of {@code GET /api/history}: the whole history, newest first.
 */
public class HistoryListResponse {

    private final boolean success = true;
    private final List<HistoryItem> data;
    private final int total;

    public HistoryListResponse(List<HistoryItem> data) {
        this.data = data;
        this.total = data.size();
    }

    public boolean isSuccess() {
        return success;
    }

    public List<HistoryItem> getData() {
        return data;
    }

    public int getTotal() {
        return total;
    }
}
