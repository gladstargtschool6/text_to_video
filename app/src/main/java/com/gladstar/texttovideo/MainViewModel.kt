package com.gladstar.texttovideo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _scenes = MutableStateFlow(
        listOf(
            Scene(
                id = 1,
                text = "Your first title",
                durationSeconds = 3,
                backgroundColor = 0xFF161B22,
                textColor = 0xFFFFFFFF,
                transitionType = TransitionType.FADE,
                textSize = 72
            ),
            Scene(
                id = 2,
                text = "Your second subtitle",
                durationSeconds = 4,
                backgroundColor = 0xFF1F2937,
                textColor = 0xFF8BE9FD,
                transitionType = TransitionType.SLIDE,
                textSize = 60
            )
        )
    )

    val scenes: StateFlow<List<Scene>> = _scenes
    val showPreview = MutableStateFlow(false)
    val selectedMusicPath = MutableStateFlow<String?>(null)

    fun addScene() {
        val nextId = (_scenes.value.maxOfOrNull { it.id } ?: 0) + 1
        _scenes.value = _scenes.value + Scene(
            id = nextId,
            text = "New scene",
            durationSeconds = 3,
            backgroundColor = 0xFF1E293B,
            textColor = 0xFFFFFFFF,
            transitionType = TransitionType.ZOOM,
            textSize = 64
        )
    }

    fun updateText(id: Int, text: String) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(text = text) else scene
        }
    }

    fun updateDuration(id: Int, duration: Int) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(durationSeconds = duration.coerceAtLeast(1)) else scene
        }
    }

    fun updateTransition(id: Int, transition: TransitionType) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(transitionType = transition) else scene
        }
    }

    fun updateTextColor(id: Int, color: Long) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(textColor = color) else scene
        }
    }

    fun updateBackgroundColor(id: Int, color: Long) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(backgroundColor = color) else scene
        }
    }

    fun updateTextSize(id: Int, size: Int) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == id) scene.copy(textSize = size.coerceIn(24, 120)) else scene
        }
    }

    fun removeScene(id: Int) {
        _scenes.value = _scenes.value.filter { it.id != id }
    }

    fun setMusicPath(path: String?) {
        selectedMusicPath.value = path
    }

    fun togglePreview() {
        showPreview.value = !showPreview.value
    }
}
