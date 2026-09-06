package com.group4.library.dto;

public class ReaderImportRowResult {
    private final int rowNumber;
    private final boolean success;
    private final String message;
    private final ReaderResponse reader;

    private ReaderImportRowResult(int rowNumber, boolean success, String message, ReaderResponse reader) {
        this.rowNumber = rowNumber;
        this.success = success;
        this.message = message;
        this.reader = reader;
    }

    public static ReaderImportRowResult success(int rowNumber, ReaderResponse reader) {
        return new ReaderImportRowResult(rowNumber, true, "Thêm thành công", reader);
    }

    public static ReaderImportRowResult failure(int rowNumber, String message) {
        return new ReaderImportRowResult(rowNumber, false, message, null);
    }

    public int getRowNumber() { return rowNumber; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ReaderResponse getReader() { return reader; }
}