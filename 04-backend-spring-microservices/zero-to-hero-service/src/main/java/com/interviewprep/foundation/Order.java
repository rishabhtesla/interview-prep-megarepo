package com.interviewprep.foundation;

/** Immutable domain value returned by the service layer. */
public record Order(long id, String item, int quantity, int totalCents, String status) {}
