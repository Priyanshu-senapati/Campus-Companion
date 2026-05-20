# 🤖 Gemini AI Integration

Campus Companion uses **Google Gemini 2.5 Flash** via a direct REST API call to power the in-app AI assistant. This document covers the integration end-to-end.

---

## Overview

The AI assistant is not a generic chatbot — it knows the student. Before every query, the app automatically injects:

| Context Injected | Source |
|-----------------|--------|
| Student name | Firestore user profile |
| Branch & semester | Firestore user profile |
| Today's timetable entries | Room DB |
| Per-subject attendance % | Room DB |
| RVU-specific rules | Hardcoded system prompt |

This allows the model to answer natural questions like:
- *"Can I skip tomorrow's lab?"* (checks attendance %)
- *"What's my weakest subject?"* (ranks by attendance %)
- *"Summarize today's schedule."* (reads timetable)

---

## API Details

| Property | Value |
|----------|-------|
| Model | `gemini-2.5-flash` |
| Endpoint | `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent` |
| Auth | API key via `local.properties` → `GEMINI_API_KEY` |
| Transport | Retrofit 2 + OkHttp 4 |
| Response | Streaming or single-shot JSON |

---

## Retrofit Interface

```kotlin
// data/remote/GeminiApiService.kt

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
```

---

## Request Data Classes

```kotlin
data class GeminiRequest(
    val system_instruction: SystemInstruction,
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class SystemInstruction(
    val parts: List<Part>
)

data class Content(
    val role: String,       // "user" or "model"
    val parts: List<Part>
)

data class Part(val text: String)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1024,
    val topP: Float = 0.95f
)
```

---

## Response Parsing

```kotlin
data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String
)

// Extract text:
val responseText = response.candidates
    .firstOrNull()
    ?.content
    ?.parts
    ?.firstOrNull()
    ?.text ?: "No response"
```

---

## System Prompt Construction

This is the most important part. The `AiAssistantRepository` builds the system prompt dynamically:

```kotlin
fun buildSystemPrompt(
    user: User,
    timetableToday: List<TimetableEntry>,
    attendanceMap: Map<String, Float>
): String {
    val scheduleText = timetableToday.joinToString("\n") {
        "- ${it.subjectName} at ${it.startTime} in ${it.room} (${it.professorName})"
    }

    val attendanceText = attendanceMap.entries.joinToString("\n") {
        "- ${it.key}: ${it.value}%"
    }

    return """
        You are Campus Companion, an AI assistant for RV University (RVU) students.
        
        STUDENT CONTEXT:
        - Name: ${user.name}
        - Branch: ${user.branch}
        - Semester: ${user.semester}
        
        TODAY'S TIMETABLE (${getCurrentDay()}):
        $scheduleText
        
        CURRENT ATTENDANCE:
        $attendanceText
        
        RVU RULES:
        - Minimum attendance required: 75% per subject
        - Below 65% = cannot appear for end-semester exam
        - Medical leave must be submitted within 3 days
        
        INSTRUCTIONS:
        - Answer questions about schedule, attendance, and campus life
        - Calculate how many classes can be missed safely
        - Be conversational, concise, and student-friendly
        - If asked about things outside your context, say so honestly
    """.trimIndent()
}
```

---

## Multi-turn Conversation

The ViewModel maintains conversation history as a `List<Content>` and sends the full history with every request:

```kotlin
class AiAssistantViewModel : AndroidViewModel(...) {
    private val conversationHistory = mutableListOf<Content>()

    fun sendMessage(userMessage: String) {
        conversationHistory.add(Content(role = "user", parts = listOf(Part(userMessage))))

        val request = GeminiRequest(
            system_instruction = SystemInstruction(listOf(Part(buildSystemPrompt(...)))),
            contents = conversationHistory.toList()
        )

        viewModelScope.launch {
            val response = repository.sendToGemini(request)
            // add model reply to history for next turn
            conversationHistory.add(Content(role = "model", parts = listOf(Part(response))))
        }
    }
}
```

---

## Error Handling

| Scenario | Behaviour |
|----------|-----------|
| No internet | `ConnectivityObserver` detects, show offline banner |
| API key missing/invalid | Catch `HttpException(401)`, show "AI unavailable" |
| Rate limit hit | Catch `HttpException(429)`, show retry with backoff |
| Empty candidates | Show fallback: "I couldn't process that, please try again" |
| Context too long | Trim oldest messages from history before sending |

---

## Security Notes

- **Never hardcode the API key** in source files
- Key is stored in `local.properties` (git-ignored) and injected via `BuildConfig.GEMINI_API_KEY` in `build.gradle.kts`
- `local.properties` is listed in `.gitignore` — verify before pushing

```kotlin
// build.gradle.kts (app module)
val geminiKey = properties["GEMINI_API_KEY"] as? String ?: ""
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
```

---

## Sample Interactions

**User:** Can I skip my afternoon lab today?

**System context injected:** Algorithms Lab attendance: 71%, 1 more absence = 69% (below 75% threshold)

**Gemini response:** *"I'd recommend not skipping today's Algorithms Lab. You're currently at 71% — one more absence brings you to ~69%, which is below the 75% minimum. You have about 2 safe absences left in the semester for that subject."*

---

## Rate Limits (Gemini 2.5 Flash, free tier)

| Limit | Value |
|-------|-------|
| Requests per minute | 15 |
| Requests per day | 1,500 |
| Tokens per minute | 1,000,000 |

For production, upgrade to a paid tier or implement per-user request throttling.
