package com.group4.library.exception;

/** Ném khi cố gắng thanh toán một khoản tiền phạt đã được thanh toán trước đó. */
public class FineAlreadyPaidException extends BusinessException {
    public FineAlreadyPaidException(String message) {
        super(message);
    }
}