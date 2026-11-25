# AI Chat Bot - Stateful & Streaming Spring Boot Application

A beginner-friendly Spring Boot 3 application using Java 17 and WebFlux to provide streaming chat responses using the official Google GenAI Java SDK (Gemini API).

## 1. Project Overview
This project demonstrates how to build an AI Chatbot with Spring Boot. Key features include:
- **Server-Sent Events (SSE)** for streaming responses.
- **Stateful Conversation Management** so the model remembers previous context.
- **Cost Protection & Validation** via `CostGuard`.
- **Response Caching** to minimize API usage costs.

---

## 2. Stateless vs. Stateful Chat
- **Stateless Chat:** Each HTTP request is treated independently. The model does not remember previous interactions. If you tell it your name in message 1, it forgets it in message 2.
- **Stateful Chat:** The application maintains context/history mapping to a session. History is accumulated and passed along with the latest user query to the Gemini API so the model maintains conversational continuity.

---

## 3. How `conversationId` Works
The client passes a unique `conversationId` string (e.g., `"session-123"`) in the request JSON payload:
1. `ChatService` retrieves or creates a `Conversation` record for the given `conversationId`.
2. The new message is appended to that conversation's history.
3. The full history of messages is formatted into Gemini `Content` objects and sent to the API.
4. If no `conversationId` is provided, a default context (`"default"`) is used.

---

## 4. Mock Mode vs. Real Mode
- **Real Mode (Default):** Calls the actual Google Gemini API (`gemini-1.5-flash`). Requires setting the environment variable `GEMINI_API_KEY`.
- **Mock Mode:** Configured strictly via `application.yml` properties. If needed for offline testing or local development without spending quota, logic is abstracted to simulate responses without making outbound external calls.

---

## 5. How to Run
Ensure you have **Java 17** installed.

1. **Set your API Key Environment Variable:**
   ```bash
   export GEMINI_API_KEY="your-google-gemini-api-key"
   # On Windows Command Prompt: set GEMINI_API_KEY="your-google-gemini-api-key"
   # On Windows PowerShell: $env:GEMINI_API_KEY="your-google-gemini-api-key"
   ```

2. **Run the Application using Maven:**
   ```bash
   mvn spring-boot:run
   ```
   *To run on a different port:*
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
   ```

---

## 6. Final API List
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/chat/stream` | Stream chat responses using SSE (supports optional `conversationId`) |
| `GET` | `/api/chat/history/{conversationId}` | Retrieve full message history for a given conversation ID |
| `DELETE` | `/api/chat/clear/{conversationId}` | Clear history for a specific conversation ID |

---

## 7. Curl Commands for PPT Demo

### A. Stateless Demo (Forgets context if conversationId is omitted/new)
```bash
# Message 1
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Hi, my name is Alice"}'

# Message 2 (New ID / No ID -> Forgets)
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"What is my name?"}'
```

### B. Stateful Demo (Remembers context using same conversationId)
```bash
# Message 1 - Introduce
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"demo-1","message":"Hi, my name is Alice"}'

# Message 2 - Ask context back
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"demo-1","message":"What is my name?"}'
```

### C. History & Clear API Demo
```bash
# Fetch History
curl -X GET http://localhost:8080/api/chat/history/demo-1

# Clear History
curl -X DELETE http://localhost:8080/api/chat/clear/demo-1

# Verify Clear
curl -X GET http://localhost:8080/api/chat/history/demo-1
```

---

## 8. Redis Upgrade Note
Currently, conversations are stored in an in-memory `ConcurrentHashMap` via `ConversationCache`. 
- **Production Consideration:** For multi-instance deployments (horizontal scaling) or persistent state across server restarts, replace the in-memory map implementation with **Spring Data Redis** (`RedisTemplate` or `ReactiveRedisTemplate`) using `conversationId` as the Redis key.

---

## 9. Travel Planner Exercise
**Exercise Task:**
Build a "Travel Planner AI Assistant" using the stateful conversation features of this app!
1. Start a stateful session (`conversationId: "trip-paris"`).
2. Tell the AI: `"I am planning a 3-day trip to Paris with a budget of $1000."`
3. Follow up with: `"Recommend places to eat based on my budget."`
4. Ask: `"Can you build a day-by-day itinerary incorporating those places?"`
5. Observe how the AI keeps track of the location (Paris), duration (3 days), and budget ($1000) across all prompts!
