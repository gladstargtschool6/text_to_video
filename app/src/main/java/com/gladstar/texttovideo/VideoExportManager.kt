package com.gladstar.texttovideo

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gladstar.texttovideo.ui.theme.TextToVideoTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember { MainViewModel() }
            val scenes by viewModel.scenes.collectAsState()

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
                        Text(
                            text = "Text to Video",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.addScene() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Add text scene")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(scenes) { scene ->
                                SceneCard(
                                    scene = scene,
                                    onTextChange = { newText -> viewModel.updateText(scene.id, newText) },
                                    onDurationChange = { newDuration -> viewModel.updateDuration(scene.id, newDuration) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.preview() },
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
                                        outputFile = outputFile
                                    )
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
}

@Composable
fun SceneCard(
    scene: Scene,
    onTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = scene.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Scene text") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duration (sec): ")
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = scene.durationSeconds.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let(onDurationChange)
                    },
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
