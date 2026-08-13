package com.group4.library.model;

public class StudentReader extends Reader {
    public StudentReader(String id, String name, String phoneNumber) {
        super(id, name, phoneNumber, ReaderType.STUDENT);
    }

    @Override
    public int getMaxBorrowLimit() {
        return 3;
    }
}
