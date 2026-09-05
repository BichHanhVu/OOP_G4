package com.group4.library.model;

/** Lớp cha trừu tượng cho các loại bạn đọc. */
public abstract class Reader extends User {
    private final ReaderType type;

    public Reader(String id, String name, String phoneNumber, ReaderType type) {
        super(id, name, phoneNumber);
        this.type = type;
    }

    public ReaderType getType() { return type; }

    public abstract int getMaxBorrowLimit();

    @Override
    public String toString() {
        return "Reader{" + super.toString() + ", type=" + type + ", maxBorrowLimit=" + getMaxBorrowLimit() + "}";
    }
}