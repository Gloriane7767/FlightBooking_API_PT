package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.tool.FlightTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor

public class OpenAiService {
    private final ChatModel chatModel;
    private final FlightTools flightTools;

    // In-memory chat history per session (max 10 messages)
    private final Map<String, List<Message>> chatHistories = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10;

    private static final String SYSTEM_MESSAGE = """
            You are a helpful flight booking assistant for a flight reservation system.
            You help users search for flights, book flights, look up their bookings, and cancel bookings.
            Always be polite, concise, and guide the user step by step.
            When a user wants to book a flight, ask for their full name and email if not provided.
            When a user wants to cancel, ask for the flight ID and their email if not provided.
            When listing flights, present them in a clear, readable format with ID, destination, departure, and price.
            If something goes wrong, explain it clearly and suggest what the user can do next.
            """;

    public String chat(String sessionId, String userMessage) {
        List<Message> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

        history.add(new UserMessage(userMessage));

        // Build the full prompt: system message + conversation history
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_MESSAGE));
        messages.addAll(history);

        ChatClient client = ChatClient.builder(chatModel).build();

        String reply = client.prompt(new Prompt(messages,
                        ToolCallingChatOptions.builder()
                                .toolCallbacks(flightTools.getToolCallbacks())
                                .build()))
                .call()
                .content();

        history.add(new AssistantMessage(reply));

        // Keep history bounded to MAX_HISTORY messages
        if (history.size() > MAX_HISTORY) {
            history.subList(0, history.size() - MAX_HISTORY).clear();
        }

        return reply;
    }
}
