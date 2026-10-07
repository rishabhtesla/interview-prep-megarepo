package com.interviewprep.progress;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProgressRequest(@NotNull Boolean completed, @Size(max = 500) String note) {}
