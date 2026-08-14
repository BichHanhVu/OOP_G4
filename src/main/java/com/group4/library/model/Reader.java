package com.group4.library.model;

public abstract class Reader extends User {
    private final ReaderType type;

    public Reader(String id, String name, String phoneNumber, ReaderType type) {
        super(id, name, phoneNumber);
        this.type = type;
    }

    public ReaderType getType() { return type; }

    public abstract int getMaxBorrowLimit();
}
