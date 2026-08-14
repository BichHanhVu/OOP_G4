package com.group4.library.model;

public interface Borrowable {
    boolean canBorrow();

    boolean canBorrow(int quantity);

    default void borrow() {
        borrow(1);
    }

    void borrow(int quantity);

    default void returnItem() {
        returnItem(1);
    }

    void returnItem(int quantity);
}