package com.example.core

import android.util.Log
import com.example.ui.theme.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val AI_TAG = "NibrasAiService"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

data class ExamHintResult(
    val eliminateIndices: List<Int>,
    val hintText: String
)

class NibrasAiService {

    // Lazy initialization per Firebase AI Logic contract (never evaluate eagerly at construction).
    private val textModel by lazy {
        Firebase.ai.generativeModel(modelName = MODEL_NAME)
    }

    private val examHintSchema = Schema.obj(
        properties = mapOf(
            "eliminateIndices" to Schema.array(
                items = Schema.integer(description = "Zero-based index of an incorrect option to eliminate"),
                description = "Two zero-based indices of clearly wrong answer options"
            ),
            "hintText" to Schema.string(description = "Short conceptual hint without revealing the final answer directly")
        )
    )

    private val examHintModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = examHintSchema
            }
        )
    }

    private fun languageInstruction(language: AppLanguage): String =
        if (language == AppLanguage.AR) {
            "Respond in clear, eloquent Modern Standard Arabic while keeping code snippets, Big-O notation, and standard CS terms in English."
        } else {
            "Respond in crisp, structured academic English with clean code snippets and Big-O notation."
        }

    fun streamTutorReply(
        userPrompt: String,
        mode: String,
        recentHistory: List<Pair<String, String>>,
        language: AppLanguage
    ): Flow<String> = flow {
        val modeDirective = when (mode) {
            "simple" -> "Explain like a friendly mentor using intuitive analogies and simple step-by-step bullets."
            "academic" -> "Use rigorous university computer science framing, formal definitions, and time/space complexity analysis."
            "code" -> "Focus on practical, runnable Python or SQL code examples with inline comments and edge-case notes."
            "summary" -> "Deliver an ultra-structured executive revision sheet with key takeaways and formulas."
            "quiz" -> "Generate 3 high-value self-test conceptual questions with hidden answers at the end."
            else -> "Be concise, clear, and academically accurate."
        }
        val historyContext = recentHistory.takeLast(6).joinToString("\n") { (role, text) ->
            "${if (role == "user") "Student" else "Nibras AI"}: ${text.take(400)}"
        }
        val prompt = buildString {
            appendLine("You are Nibras AI (مساعد نبراس الذكي), an expert CS & AI university tutor.")
            appendLine(languageInstruction(language))
            appendLine("Mode instruction: $modeDirective")
            if (historyContext.isNotBlank()) {
                appendLine("\nRecent conversation:\n$historyContext")
            }
            appendLine("\nStudent question:\n$userPrompt")
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(AI_TIMEOUT_MS) {
                textModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Tutor streaming timed out", e)
            throw IllegalStateException("Request timed out after 60s. Please retry.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Tutor streaming failed", e)
            throw e
        }
    }

    fun streamLessonSummary(
        courseTitle: String,
        lessonTitle: String,
        lessonContent: String,
        codeSnippet: String,
        language: AppLanguage
    ): Flow<String> = flow {
        val prompt = buildString {
            appendLine("You are Nibras Smart Lesson Summarizer.")
            appendLine(languageInstruction(language))
            appendLine("Create a concise, structured summary for this technical lesson with: 1) Core Concept, 2) Key Takeaways (3 bullets), 3) Practical Exam Tip.")
            appendLine("Course: $courseTitle")
            appendLine("Lesson: $lessonTitle")
            appendLine("Content: $lessonContent")
            if (codeSnippet.isNotBlank()) {
                appendLine("Code:\n$codeSnippet")
            }
        }
        streamPrompt(prompt).collect { emit(it) }
    }

    fun streamBookAssistant(
        bookTitle: String,
        passage: String,
        action: String,
        language: AppLanguage
    ): Flow<String> = flow {
        val actionPrompt = when (action) {
            "explain" -> "Explain this textbook passage clearly with a short real-world software engineering example."
            "translate" -> if (language == AppLanguage.AR) {
                "Translate this passage accurately into academic Arabic and explain any technical terms."
            } else {
                "Translate/paraphrase this passage into clear academic English and highlight key technical concepts."
            }
            "quiz" -> "Create 2 quick comprehension questions with brief answers based on this passage."
            else -> "Summarize the key insight in this passage in 3 crisp bullet points."
        }
        val prompt = buildString {
            appendLine("You are Nibras Smart Book Copilot.")
            appendLine(languageInstruction(language))
            appendLine("Book: $bookTitle")
            appendLine("Task: $actionPrompt")
            appendLine("Passage:\n$passage")
        }
        streamPrompt(prompt).collect { emit(it) }
    }

    suspend fun generateExamHint(
        questionText: String,
        codeSnippet: String?,
        options: List<String>,
        correctIndex: Int,
        language: AppLanguage
    ): Result<ExamHintResult> {
        return try {
            val formattedOptions = options.mapIndexed { idx, opt -> "[$idx] $opt" }.joinToString("\n")
            val prompt = buildString {
                appendLine("You are Nibras Exam 50/50 Hint Assistant.")
                appendLine(languageInstruction(language))
                appendLine("Question: $questionText")
                if (!codeSnippet.isNullOrBlank()) {
                    appendLine("Code:\n$codeSnippet")
                }
                appendLine("Options:\n$formattedOptions")
                appendLine("Note: The correct option index is $correctIndex. Pick 2 OTHER indices (never $correctIndex) to eliminate in eliminateIndices, and provide a short conceptual hintText that guides the student without directly stating the answer letter.")
            }
            val response = withTimeout(AI_TIMEOUT_MS) {
                examHintModel.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("Empty hint response from model.")
            val json = JSONObject(rawJson)
            val arr = json.getJSONArray("eliminateIndices")
            val eliminated = (0 until arr.length())
                .map { arr.getInt(it) }
                .filter { it in options.indices && it != correctIndex }
                .distinct()
                .take(2)
            val hint = json.getString("hintText")
            Result.success(ExamHintResult(eliminated, hint))
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Exam hint timed out", e)
            Result.failure(IllegalStateException("Hint request timed out.", e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Exam hint failed", e)
            Result.failure(e)
        }
    }

    fun streamExamDiagnostics(
        examTitle: String,
        scorePercent: Int,
        correctCount: Int,
        totalQuestions: Int,
        missedTopics: List<String>,
        language: AppLanguage
    ): Flow<String> = flow {
        val prompt = buildString {
            appendLine("You are Nibras AI Academic Diagnostics Coach.")
            appendLine(languageInstruction(language))
            appendLine("Analyze this student's exam result and output: 1) Strength Assessment, 2) Weak Areas to Review, 3) Recommended Next 48-Hour Action Plan.")
            appendLine("Exam: $examTitle")
            appendLine("Score: $scorePercent% ($correctCount / $totalQuestions)")
            appendLine("Missed topics: ${if (missedTopics.isEmpty()) "None — perfect score!" else missedTopics.joinToString(", ")}")
        }
        streamPrompt(prompt).collect { emit(it) }
    }

    fun streamStudyPlan(
        trackTitle: String,
        milestones: List<String>,
        weeklyHours: Int,
        pace: String,
        language: AppLanguage
    ): Flow<String> = flow {
        val prompt = buildString {
            appendLine("You are Nibras AI Personalized Study Planner.")
            appendLine(languageInstruction(language))
            appendLine("Generate a practical 4-week milestone study schedule tailored to the student's availability.")
            appendLine("Learning Track: $trackTitle")
            appendLine("Upcoming Milestones: ${milestones.joinToString(" -> ")}")
            appendLine("Available Weekly Hours: $weeklyHours hours/week")
            appendLine("Preferred Pace: $pace")
            appendLine("Format cleanly with Week 1 to Week 4 sections, daily focus blocks, and measurable weekly goals.")
        }
        streamPrompt(prompt).collect { emit(it) }
    }

    suspend fun generateCommunityDigest(
        postTitle: String,
        postBody: String,
        codeSnippet: String?,
        language: AppLanguage
    ): Result<String> {
        return try {
            val prompt = buildString {
                appendLine("You are Nibras Community AI Digest.")
                appendLine(languageInstruction(language))
                appendLine("Provide a crisp 3-line technical insight and best-practice answer to this student discussion post:")
                appendLine("Title: $postTitle")
                appendLine("Body: $postBody")
                if (!codeSnippet.isNullOrBlank()) {
                    appendLine("Code:\n$codeSnippet")
                }
            }
            val response = withTimeout(AI_TIMEOUT_MS) {
                textModel.generateContent(prompt)
            }
            val text = response.text
            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("Empty digest from Gemini."))
            } else {
                Result.success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Community digest timed out", e)
            Result.failure(IllegalStateException("Digest timed out.", e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Community digest failed", e)
            Result.failure(e)
        }
    }

    private fun streamPrompt(prompt: String): Flow<String> = flow {
        val sb = StringBuilder()
        try {
            withTimeout(AI_TIMEOUT_MS) {
                textModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        sb.append(piece)
                        emit(sb.toString())
                    }
                }
            }
            if (sb.isBlank()) {
                throw IllegalStateException("Model returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "AI stream timed out", e)
            throw IllegalStateException("AI request timed out after 60 seconds.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "AI stream failed", e)
            throw e
        }
    }
}
