package com.group4.library.dto;

import java.time.LocalDate;

public class RenewTicketRequest {
    private LocalDate newDueDate;

    public RenewTicketRequest() {
    }

    public RenewTicketRequest(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
    }

    public LocalDate getNewDueDate() {
        return newDueDate;
    }

    public void setNewDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
    }
}