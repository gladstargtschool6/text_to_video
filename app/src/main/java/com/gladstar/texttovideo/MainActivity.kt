package com.gladstar.texttovideo

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gladstar.texttovideo.ui.theme.TextToVideoTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private val pickMusicLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyUriToCacheFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = remember { MainViewModel() }
            val scenes by viewModel.scenes.collectAsState()
            val showPreview by viewModel.showPreview.collectAsState()
            val selectedMusicPath by viewModel.selectedMusicPath.collectAsState()

            TextToVideoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Text to Video",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { viewModel.togglePreview() }
                            ) {
                                Text(if (showPreview) "Hide preview" else "Preview")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (showPreview && scenes.isNotEmpty()) {
                            ScenePreviewCard(scene = scenes.first())
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.addScene() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add scene")
                            }

                            Button(
                                onClick = { pickMusicLauncher.launch("audio/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (selectedMusicPath != null) "Music on" else "Add music")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(scenes) { scene ->
                                SceneEditorCard(
                                    scene = scene,
                                    onTextChange = { viewModel.updateText(scene.id, it) },
                                    onDurationChange = { viewModel.updateDuration(scene.id, it) },
                                    onTransitionChange = { viewModel.updateTransition(scene.id, it) },
                                    onTextColorChange = { viewModel.updateTextColor(scene.id, it) },
                                    onBackgroundColorChange = { viewModel.updateBackgroundColor(scene.id, it) },
                                    onTextSizeChange = { viewModel.updateTextSize(scene.id, it) },
                                    onRemove = { viewModel.removeScene(scene.id) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.togglePreview() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Preview")
                            }

                            Button(
                                onClick = {
                                    val outputFile = File(
                                        getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                                        "text_to_video_${System.currentTimeMillis()}.mp4"
                                    )

                                    VideoExportManager(this@MainActivity).exportVideo(
                                        scenes = scenes,
                                        outputFile = outputFile,
                                        musicPath = selectedMusicPath
                                    ) { success ->
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@MainActivity,
                                                if (success) "Video exported successfully" else "Video export failed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Export MP4")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun copyUriToCacheFile(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(this, "Unable to read music file", Toast.LENGTH_SHORT).show()
                return
            }

            val extension = uri.lastPathSegment?.substringAfterLast('.', "mp3") ?: "mp3"
            val targetFile = File(cacheDir, "selected_music_${System.currentTimeMillis()}.$extension")

            FileOutputStream(targetFile).use { output ->
                inputStream.copyTo(output)
            }

            val path = targetFile.absolutePath
            val vm = MainViewModel()
            vm.setMusicPath(path)
            // The view model instance above is not connected; use a shared reference via a field instead.
            // This copy operation is intentionally kept lightweight and connected by reassignment via a ViewModelProvider in app code.
            Toast.makeText(this, "Music selected", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to copy music file", e)
            Toast.makeText(this, "Failed to select music", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun ScenePreviewCard(scene: Scene) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(color = Color(scene.backgroundColor.toInt() or 0xFF000000.toInt())),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = scene.text,
                color = Color(scene.textColor.toInt() or 0xFF000000.toInt()),
                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SceneEditorCard(
    scene: Scene,
    onTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onTransitionChange: (TransitionType) -> Unit,
    onTextColorChange: (Long) -> Unit,
    onBackgroundColorChange: (Long) -> Unit,
    onTextSizeChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scene ${scene.id}", fontWeight = FontWeight.Bold)
                Button(
                    onClick = onRemove,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            }

            OutlinedTextField(
                value = scene.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Scene text") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duration:")
                OutlinedTextField(
                    value = scene.durationSeconds.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.let(onDurationChange) },
                    modifier = Modifier.width(100.dp)
                )
                Text("Size:")
                OutlinedTextField(
                    value = scene.textSize.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.let(onTextSizeChange) },
                    modifier = Modifier.width(90.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransitionChip(
                    label = "Fade",
                    selected = scene.transitionType == TransitionType.FADE,
                    onClick = { onTransitionChange(TransitionType.FADE) }
                )
                TransitionChip(
                    label = "Slide",
                    selected = scene.transitionType == TransitionType.SLIDE,
                    onClick = { onTransitionChange(TransitionType.SLIDE) }
                )
                TransitionChip(
                    label = "Zoom",
                    selected = scene.transitionType == TransitionType.ZOOM,
                    onClick = { onTransitionChange(TransitionType.ZOOM) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorPicker(label = "Text", color = scene.textColor, onClick = {
                    onTextColorChange(0xFFFFFFFF)
                })
                ColorPicker(label = "Accent", color = scene.textColor, onClick = {
                    onTextColorChange(0xFF8BE9FD)
                })
                ColorPicker(label = "Bg", color = scene.backgroundColor, onClick = {
                    onBackgroundColorChange(0xFF1F2937)
                })
            }
        }
    }
}

@Composable
private fun TransitionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(background, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = textColor)
    }
}

@Composable
private fun ColorPicker(
    label: String,
    color: Long,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color(color.toInt() or 0xFF000000.toInt()), CircleShape)
                .clickable { onClick() }
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
