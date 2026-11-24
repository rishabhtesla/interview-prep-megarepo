package com.interviewprep.foundation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotBlank @Size(max = 80) String item,
        @Positive int quantity,
        @Positive int unitPriceCents) {}
