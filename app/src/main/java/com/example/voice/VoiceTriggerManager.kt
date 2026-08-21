package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceTriggerManager(
    context: Context,
    private val onKeywordMatched: (String) -> Unit
) : RecognitionListener {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var expectedKeyword = ""
    private var onHeardPhrase: ((String) -> Unit)? = null
    private var isListening = false

    private val _listeningState = MutableStateFlow(false)
    val listeningState: StateFlow<Boolean> = _listeningState.asStateFlow()

    fun startListening(expectedKeyword: String, onHeardPhrase: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) return

        this.expectedKeyword = normalize(expectedKeyword)
        this.onHeardPhrase = onHeardPhrase

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                setRecognitionListener(this@VoiceTriggerManager)
            }
        }

        if (isListening) return

        isListening = true
        _listeningState.value = true
        restartListening()
    }

    fun stopListening() {
        isListening = false
        _listeningState.value = false
        speechRecognizer?.cancel()
    }

    private fun restartListening() {
        val recognizer = speechRecognizer ?: return
        if (!isListening) return

        recognizer.cancel()
        recognizer.startListening(createRecognizerIntent())
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }
    }

    private fun normalize(value: String): String {
        return value.trim().uppercase(Locale.getDefault()).filter { it.isLetterOrDigit() || it.isWhitespace() }
            .replace("\\s+".toRegex(), " ")
    }

    private fun handleRecognizedText(bundle: Bundle?) {
        val matches = bundle
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            .orEmpty()

        for (phrase in matches) {
            onHeardPhrase?.invoke(phrase)
            if (matchesExpectedKeyword(phrase)) {
                onKeywordMatched(phrase)
                break
            }
        }
    }

    private fun matchesExpectedKeyword(phrase: String): Boolean {
        val normalizedPhrase = normalize(phrase)
        if (normalizedPhrase.isBlank() || expectedKeyword.isBlank()) return false

        if (normalizedPhrase == expectedKeyword) return true

        val wordBoundaryPattern = "(^|\\s)${Regex.escape(expectedKeyword)}(\\s|$)".toRegex()
        return wordBoundaryPattern.containsMatchIn(normalizedPhrase)
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        if (isListening) {
            mainHandler.postDelayed({ restartListening() }, 400L)
        }
    }

    override fun onError(error: Int) {
        if (isListening) {
            mainHandler.postDelayed({ restartListening() }, 500L)
        }
    }

    override fun onResults(results: Bundle?) {
        handleRecognizedText(results)
        if (isListening) {
            mainHandler.postDelayed({ restartListening() }, 350L)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        handleRecognizedText(partialResults)
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}