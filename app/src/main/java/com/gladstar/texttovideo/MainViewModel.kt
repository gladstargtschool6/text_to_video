package com.gladstar.texttovideo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _scenes = MutableStateFlow(
        listOf(
            Scene(1, "Your first title", 3, 0xFFFFFFFF, 0xFF161B22, TransitionType.FADE, 72),
            Scene(2, "Your second subtitle", 4, 0xFF8BE9FD, 0xFF1F2937, TransitionType.SLIDE, 60)
        )
    )
    val scenes: StateFlow<List<Scene>> = _scenes

    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    private val _selectedMusicPath = MutableStateFlow<String?>(null)
    val selectedMusicPath: StateFlow<String?> = _selectedMusicPath

    fun addScene() {
        val id = (_scenes.value.maxOfOrNull { it.id } ?: 0) + 1
        _scenes.value += Scene(id, "New scene")
    }

    fun updateText(id: Int, value: String) = update(id) { it.copy(text = value) }
    fun updateDuration(id: Int, value: Int) = update(id) { it.copy(durationSeconds = value.coerceIn(1, 60)) }
    fun updateTransition(id: Int, value: TransitionType) = update(id) { it.copy(transitionType = value) }
    fun updateTextColor(id: Int, value: Long) = update(id) { it.copy(textColor = value) }
    fun updateBackgroundColor(id: Int, value: Long) = update(id) { it.copy(backgroundColor = value) }
    fun updateTextSize(id: Int, value: Int) = update(id) { it.copy(textSize = value.coerceIn(24, 120)) }
    fun removeScene(id: Int) { _scenes.value = _scenes.value.filterNot { it.id == id } }
    fun togglePreview() { _showPreview.value = !_showPreview.value }
    fun setMusicPath(path: String?) { _selectedMusicPath.value = path }

    private fun update(id: Int, transform: (Scene) -> Scene) {
        _scenes.value = _scenes.value.map { if (it.id == id) transform(it) else it }
    }
}
