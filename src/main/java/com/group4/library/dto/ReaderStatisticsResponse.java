// dto/ReaderStatisticsResponse.java
package com.group4.library.dto;

import java.util.Map;

public class ReaderStatisticsResponse {
    private final long totalReaders;
    private final Map<String, Long> countByType;
    private final long currentlyBorrowingReaderCount;
    private final long overdueReaderCount;
    private final long reachedLimitReaderCount;

    public ReaderStatisticsResponse(long totalReaders, Map<String, Long> countByType,
                                    long currentlyBorrowingReaderCount, long overdueReaderCount,
                                    long reachedLimitReaderCount) {
        this.totalReaders = totalReaders;
        this.countByType = countByType;
        this.currentlyBorrowingReaderCount = currentlyBorrowingReaderCount;
        this.overdueReaderCount = overdueReaderCount;
        this.reachedLimitReaderCount = reachedLimitReaderCount;
    }

    public long getTotalReaders() { return totalReaders; }
    public Map<String, Long> getCountByType() { return countByType; }
    public long getCurrentlyBorrowingReaderCount() { return currentlyBorrowingReaderCount; }
    public long getOverdueReaderCount() { return overdueReaderCount; }
    public long getReachedLimitReaderCount() { return reachedLimitReaderCount; }
}