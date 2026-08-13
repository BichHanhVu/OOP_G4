package com.group4.library.model;

public class LecturerReader extends Reader {
    public LecturerReader(String id, String name, String phoneNumber) {
        super(id, name, phoneNumber, ReaderType.LECTURER);
    }

    @Override
    public int getMaxBorrowLimit() {
        return 7;
    }
}
