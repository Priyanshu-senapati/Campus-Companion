package com.rvu.campuscompanion.data.repository

import com.rvu.campuscompanion.BuildConfig
import com.rvu.campuscompanion.data.remote.GeminiContent
import com.rvu.campuscompanion.data.remote.GeminiPart
import com.rvu.campuscompanion.data.remote.GeminiRequest
import com.rvu.campuscompanion.data.remote.RetrofitClient
import com.rvu.campuscompanion.data.local.SubjectAttendance
import com.rvu.campuscompanion.data.local.TimetableEntry

class AssistantRepository(
    private val attendanceRepo: AttendanceRepository,
    private val timetableRepo: TimetableRepository
) {
    private val api = RetrofitClient.geminiApi
    private val model = "gemini-2.5-flash"

    suspend fun ask(prompt: String, studentName: String, branch: String, semester: Int): Result<String> = runCatching {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY.startsWith("YOUR_")) {
            error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties.")
        }

        val today = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH)
            .format(java.util.Date())
        val attendance = runCatching { attendanceRepo.summarySync() }.getOrDefault(emptyList())
        val todayClasses = runCatching { timetableRepo.getByDaySync(today) }.getOrDefault(emptyList())

        val systemContext = buildSystemContext(studentName, branch, semester, today, attendance, todayClasses)

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(systemContext + "\n\nStudent: " + prompt)))
            )
        )
        val resp = api.generateContent(model, BuildConfig.GEMINI_API_KEY, request)
        resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            ?: "I couldn't generate a response. Please try again."
    }

    private fun buildSystemContext(
        name: String,
        branch: String,
        semester: Int,
        today: String,
        attendance: List<SubjectAttendance>,
        todayClasses: List<TimetableEntry>
    ): String = buildString {
        appendLine("You are RVU Assistant, a helpful AI for students at RV University, Bengaluru.")
        appendLine("Be friendly, concise (2-4 sentences), and use the context below to answer.")
        appendLine("If asked something not in the context, answer based on general knowledge but keep it brief.")
        appendLine()
        appendLine("=== STUDENT CONTEXT ===")
        appendLine("Name: $name")
        appendLine("Branch: $branch, Semester: $semester")
        appendLine("Today: $today")
        appendLine()
        if (todayClasses.isNotEmpty()) {
            appendLine("Today's classes:")
            todayClasses.forEach {
                appendLine("- ${it.startTime}-${it.endTime}: ${it.subject} (${it.type}) in ${it.room} with ${it.professor}")
            }
        } else {
            appendLine("No classes scheduled today.")
        }
        appendLine()
        if (attendance.isNotEmpty()) {
            appendLine("Attendance summary:")
            attendance.forEach {
                appendLine("- ${it.subject}: ${"%.1f".format(it.percentage)}% (${it.present}/${it.total})")
            }
        } else {
            appendLine("No attendance records yet.")
        }
        appendLine()
        appendLine("Note: 75% attendance is mandatory at RVU.")
    }
}
