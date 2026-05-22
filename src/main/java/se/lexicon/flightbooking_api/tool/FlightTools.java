package se.lexicon.flightbooking_api.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.FlightBookingDTO;
import se.lexicon.flightbooking_api.dto.FlightListDTO;
import se.lexicon.flightbooking_api.service.FlightBookingService;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor

public class FlightTools {
    private final FlightBookingService flightBookingService;

    @Tool(description = "Get a list of all flights in the system including their ID, destination, departure time, and price")
    public List<FlightListDTO> getAllFlights() {
        return flightBookingService.findAll();
    }

    @Tool(description = "Get a list of only available flights that can currently be booked")
    public List<AvailableFlightDTO> getAvailableFlights() {
        return flightBookingService.findAvailableFlights();
    }

    @Tool(description = "Book a flight for a passenger. Requires the flight ID, passenger full name, and passenger email")
    public FlightBookingDTO bookFlight(Long flightId, String passengerName, String passengerEmail) {
        return flightBookingService.bookFlight(flightId, new BookFlightRequestDTO(passengerName, passengerEmail));
    }

    @Tool(description = "Cancel an existing flight booking. Requires the flight ID and the passenger email used when booking")
    public String cancelFlight(Long flightId, String passengerEmail) {
        flightBookingService.cancelFlight(flightId, passengerEmail);
        return "Booking for flight " + flightId + " has been successfully cancelled.";
    }

    @Tool(description = "Look up all bookings associated with a given passenger email address")
    public List<FlightBookingDTO> getBookingsByEmail(String email) {
        return flightBookingService.findBookingsByEmail(email);
    }

    // Expose all @Tool methods as ToolCallback array for ChatClient
    public ToolCallback[] getToolCallbacks() {
        return Arrays.stream(MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks())
                .toArray(ToolCallback[]::new);
    }
}
