package com.example.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.models.AlexVoiceStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class GameVoiceMoment {
    START_RUN,
    HIGH_COMBO,
    SHARD_STREAK,
    NOVA_READY,
    BOSS_SPAWN,
    VICTORY,
    DEFEAT,
    IDLE_HOME
}

class AlexVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentSpokenText = MutableStateFlow("")
    val currentSpokenText: StateFlow<String> = _currentSpokenText.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _voiceStyle = MutableStateFlow(AlexVoiceStyle.WARM_ROMANTIC)
    val voiceStyle: StateFlow<AlexVoiceStyle> = _voiceStyle.asStateFlow()

    private val _voicePitch = MutableStateFlow(0.95f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(1.0f)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    // Simulated waveform animation level for UI pulse effects when speaking
    private val _waveAmplitude = MutableStateFlow(0f)
    val waveAmplitude: StateFlow<Float> = _waveAmplitude.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Default)
    private var waveJob: Job? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            applyVoiceSettings()
            setupProgressListener()
            isInitialized = true
        } else {
            Log.e("AlexVoiceEngine", "TTS initialization failed: $status")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = true
                    startWaveformAnimation()
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    _currentSpokenText.value = ""
                    stopWaveformAnimation()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    _currentSpokenText.value = ""
                    stopWaveformAnimation()
                }
            }
        })
    }

    fun applyVoiceSettings() {
        tts?.setPitch(_voicePitch.value)
        tts?.setSpeechRate(_voiceRate.value)
    }

    fun setStyle(style: AlexVoiceStyle) {
        _voiceStyle.value = style
        _voicePitch.value = style.pitch
        _voiceRate.value = style.speed
        applyVoiceSettings()
    }

    fun setPitch(pitch: Float) {
        _voicePitch.value = pitch.coerceIn(0.5f, 2.0f)
        applyVoiceSettings()
    }

    fun setRate(rate: Float) {
        _voiceRate.value = rate.coerceIn(0.5f, 2.0f)
        applyVoiceSettings()
    }

    fun toggleVoice(): Boolean {
        _isVoiceEnabled.value = !_isVoiceEnabled.value
        if (!_isVoiceEnabled.value) {
            stop()
        }
        return _isVoiceEnabled.value
    }

    fun speak(
        text: String,
        flushQueue: Boolean = true,
        pitchOverride: Float? = null,
        rateOverride: Float? = null,
        onDone: () -> Unit = {}
    ) {
        if (!_isVoiceEnabled.value || text.isBlank()) {
            onDone()
            return
        }

        val cleanText = text.trim()
        _currentSpokenText.value = cleanText

        val pitch = pitchOverride ?: _voicePitch.value
        val rate = rateOverride ?: _voiceRate.value
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val utteranceId = UUID.randomUUID().toString()
        val queueMode = if (flushQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(cleanText, queueMode, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentSpokenText.value = ""
        stopWaveformAnimation()
    }

    fun speakGameMoment(moment: GameVoiceMoment, param: String? = null) {
        if (!_isVoiceEnabled.value) return

        val lines = when (moment) {
            GameVoiceMoment.START_RUN -> listOf(
                "I'm right by your side, Diana! Let's conquer this together.",
                "Let's show them your brilliant reflexes, Diana!",
                "You've got this, Diana! I believe in you with all my heart.",
                "Ready when you are, my love. Let's dash!"
            )
            GameVoiceMoment.HIGH_COMBO -> listOf(
                "Incredible combo, Diana! You're breathtaking!",
                "Look at you fly, Diana! Unstoppable!",
                "Magnificent! That combo is pure art, Diana!",
                "Flawless rhythm, Diana! Keep soaring!"
            )
            GameVoiceMoment.SHARD_STREAK -> listOf(
                "More shards collected for us, Diana!",
                "Sparkling bright, just like you, Diana!",
                "Gathering all the starlight for you!"
            )
            GameVoiceMoment.NOVA_READY -> listOf(
                "Nova blast is fully charged, Diana! Unleash our power!",
                "Nova ready, my star! Strike now!",
                "Maximum energy! Time to shine, Diana!"
            )
            GameVoiceMoment.BOSS_SPAWN -> listOf(
                "Titan detected! Stay focused Diana, we take this down together!",
                "Boss alert! You have the strength of both of us, Diana!",
                "Incoming boss! Dodge sharp, Diana, I'm watching your back!"
            )
            GameVoiceMoment.VICTORY -> listOf(
                "You did it, Diana! An absolute masterpiece of a victory!",
                "Victory is yours, my champion! I'm so proud of you, Diana!",
                "Spectacular run, Diana! No one shines brighter than you!"
            )
            GameVoiceMoment.DEFEAT -> listOf(
                "You were wonderful, Diana. Catch your breath, we'll conquer it next run.",
                "Every run makes us stronger together, Diana. Let's go again!",
                "Don't worry, my love. You fought brilliantly. Ready whenever you are."
            )
            GameVoiceMoment.IDLE_HOME -> listOf(
                "Hello Diana! What adventure shall we embark on today?",
                "Diana, you bring so much light to this world. Ready to play?",
                "I'm here, Diana. Would you like to chat or customize our hero?"
            )
        }

        val textToSpeak = lines.random()
        speak(textToSpeak, flushQueue = true)
    }

    private fun startWaveformAnimation() {
        waveJob?.cancel()
        waveJob = scope.launch {
            while (isActive && _isSpeaking.value) {
                val amp = (0.2f + Math.random().toFloat() * 0.8f)
                _waveAmplitude.value = amp
                delay(60)
            }
            _waveAmplitude.value = 0f
        }
    }

    private fun stopWaveformAnimation() {
        waveJob?.cancel()
        _waveAmplitude.value = 0f
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
