package com.gladstar.texttovideo

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

class VideoExportManager(private val context: Context) {

    fun exportVideo(
        scenes: List<Scene>,
        outputFile: File,
        musicPath: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val safeScenes = scenes.filter { it.text.isNotBlank() }
        if (safeScenes.isEmpty()) {
            onComplete(false)
            return
        }

        outputFile.parentFile?.mkdirs()

        val totalDuration = safeScenes.sumOf { it.durationSeconds }.toDouble()
        val filters = buildTextFilters(safeScenes)

        val command = if (musicPath.isNullOrBlank()) {
            mutableListOf(
                "-y",
                "-f",
                "lavfi",
                "-i",
                "color=#1D1D1F:s=1280x720:d=${totalDuration}",
                "-vf",
                filters.joinToString(","),
                "-c:v",
                "libx264",
                "-pix_fmt",
                "yuv420p",
                outputFile.absolutePath
            )
        } else {
            mutableListOf(
                "-y",
                "-i",
                musicPath,
                "-f",
                "lavfi",
                "-i",
                "color=#1D1D1F:s=1280x720:d=${totalDuration}",
                "-filter_complex",
                "[1:v]${filters.joinToString(",")} [v];[0:a]aformat=sample_rates=44100:channel_layouts=stereo,apad,volume=1.0[a]",
                "-map",
                "[v]",
                "-map",
                "[a]",
                "-c:v",
                "libx264",
                "-c:a",
                "aac",
                "-shortest",
                outputFile.absolutePath
            )
        }

        Log.d("VideoExportManager", "FFmpeg command: ${command.joinToString(" ")}")

        FFmpegKit.executeAsync(command.toTypedArray()) { session ->
            val success = ReturnCode.isSuccess(session.returnCode)
            Log.d("VideoExportManager", "FFmpeg export success=$success rc=${session.returnCode}")
            onComplete(success)
        }
    }

    private fun buildTextFilters(scenes: List<Scene>): List<String> {
        val filters = mutableListOf<String>()
        var cursor = 0.0

        scenes.forEach { scene ->
            val start = cursor
            val end = cursor + scene.durationSeconds
            val textColor = scene.textColor.toHexColor()
            val bgColor = scene.backgroundColor.toHexColor()

            val transitionExpression = when (scene.transitionType) {
                TransitionType.FADE -> {
                    "alpha='if(lt(t-$start,0.5), (t-$start)/0.5, if(gt(t-$start,${scene.durationSeconds - 0.5}), 1-((t-$start)-(${scene.durationSeconds - 0.5}))/0.5, 1))'"
                }
                TransitionType.SLIDE -> {
                    "x='(w-text_w)/2 + if(lt(t-$start,0.5), -200*(0.5-(t-$start))/0.5, if(gt(t-$start,${scene.durationSeconds - 0.5}), 200*((t-$start-(${scene.durationSeconds - 0.5}))/0.5), 0))'"
                }
                TransitionType.ZOOM -> {
                    "fontsize='if(lt(t-$start,0.5), ${scene.textSize - 8}, if(gt(t-$start,${scene.durationSeconds - 0.5}), ${scene.textSize - 8}, ${scene.textSize}))'"
                }
            }

            val textFilter = "drawtext=fontfile=/system/fonts/Roboto-Regular.ttf:text='${escape(scene.text)}':fontcolor=${textColor}:fontsize=${scene.textSize}:x=(w-text_w)/2:y=(h-text_h)/2:${transitionExpression}:enable='between(t,$start,$end)'"
            filters.add(textFilter)
            cursor = end.toDouble()
        }

        return filters
    }

    private fun Long.toHexColor(): String {
        val color = this and 0xFFFFFFFFL
        return String.format("#%06X", color.toInt())
    }

    private fun escape(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace(":", "\\:")
    }
}
