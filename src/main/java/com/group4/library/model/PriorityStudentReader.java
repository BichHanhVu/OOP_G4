package com.group4.library.model;

public class PriorityStudentReader extends Reader {
    public PriorityStudentReader(String id, String name, String phoneNumber) {
        super(id, name, phoneNumber, ReaderType.PRIORITY_STUDENT);
    }

    @Override
    public int getMaxBorrowLimit() {
        return 5;
    }
}
