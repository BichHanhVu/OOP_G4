package com.group4.library.policy;

public class LecturerFinePolicy implements FinePolicy {
    private static final long RATE_PER_DAY = 2_000L;

    @Override
    public long calculateFine(long lateDays) {
        return Math.max(0, lateDays) * RATE_PER_DAY;
    }
}
