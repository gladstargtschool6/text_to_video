package com.gladstar.texttovideo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _scenes = MutableStateFlow(
        listOf(
            Scene(id = 1, text = "Your first title", durationSeconds = 3),
            Scene(id = 2, text = "Your second subtitle", durationSeconds = 4)
        )
    )

    val scenes: StateFlow<List<Scene>> = _scenes

    fun addScene() {
        val nextId = (_scenes.value.maxOfOrNull { it.id } ?: 0) + 1
        _scenes.value = _scenes.value + Scene(id = nextId, text = "New scene", durationSeconds = 3)
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

    fun preview() {
        // Placeholder preview logic for the MVP.
    }
}
