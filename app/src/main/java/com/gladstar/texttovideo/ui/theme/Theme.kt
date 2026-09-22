package com.gladstar.texttovideo

data class Scene(
    val id: Int,
    val text: String,
    val durationSeconds: Int = 3,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0xFF1F1F1F
)
