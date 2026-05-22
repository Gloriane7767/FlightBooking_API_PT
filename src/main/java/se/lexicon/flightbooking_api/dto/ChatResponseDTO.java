package se.lexicon.flightbooking_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@NotNull(message = "Reply cannot be null")
public record ChatResponseDTO(String reply) {
}
