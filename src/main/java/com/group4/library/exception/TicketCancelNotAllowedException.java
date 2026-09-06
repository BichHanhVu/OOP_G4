package com.group4.library.exception;

/** Ném khi phiếu mượn không đủ điều kiện để hủy (đã trả hoặc đã hủy trước đó). */
public class TicketCancelNotAllowedException extends BusinessException {
    public TicketCancelNotAllowedException(String message) {
        super(message);
    }
}