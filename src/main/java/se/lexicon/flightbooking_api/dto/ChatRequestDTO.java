package se.lexicon.flightbooking_api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDTO(
        @NotBlank(message = "Session ID is required")
        String sessionId,
        @NotBlank(message = "Message is required")
        String message
) {
}
