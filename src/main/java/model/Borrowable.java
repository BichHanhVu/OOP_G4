package model;

public interface Borrowable {
    boolean canBorrow();

    void borrow();

    void returnItem();
}