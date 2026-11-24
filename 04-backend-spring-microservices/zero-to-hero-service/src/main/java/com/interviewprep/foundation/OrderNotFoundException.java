package com.interviewprep.foundation;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order " + id + " was not found");
    }
}
