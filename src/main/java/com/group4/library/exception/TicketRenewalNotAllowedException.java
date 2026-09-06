package com.group4.library.exception;

/** Ném khi phiếu mượn không đủ điều kiện để gia hạn (đã quá hạn, đã hết lượt gia hạn, ...). */
public class TicketRenewalNotAllowedException extends BusinessException {
    public TicketRenewalNotAllowedException(String message) {
        super(message);
    }
}