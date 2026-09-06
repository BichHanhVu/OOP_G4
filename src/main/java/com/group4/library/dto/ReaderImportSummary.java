package com.group4.library.dto;

import java.util.List;

public class ReaderImportSummary {
    private final int totalRows;
    private final int successCount;
    private final int failureCount;
    private final List<ReaderImportRowResult> results;

    public ReaderImportSummary(List<ReaderImportRowResult> results) {
        this.results = results;
        this.totalRows = results.size();
        this.successCount = (int) results.stream().filter(ReaderImportRowResult::isSuccess).count();
        this.failureCount = totalRows - successCount;
    }

    public int getTotalRows() { return totalRows; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public List<ReaderImportRowResult> getResults() { return results; }
}