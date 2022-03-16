package net.yanzm.sample.camerax.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import net.yanzm.sample.camerax.data.CamaraProviderAndCapabilities
import net.yanzm.sample.camerax.data.CameraCapability
import net.yanzm.sample.camerax.data.SupportedQuality
import net.yanzm.sample.camerax.data.getCamaraProviderAndCapabilities
import net.yanzm.sample.camerax.ui.component.AudioCheckbox
import net.yanzm.sample.camerax.ui.component.CameraSwitchButton
import net.yanzm.sample.camerax.ui.component.PauseButton
import net.yanzm.sample.camerax.ui.component.QualityList
import net.yanzm.sample.camerax.ui.component.ResumeButton
import net.yanzm.sample.camerax.ui.component.StartButton
import net.yanzm.sample.camerax.ui.component.StopButton
import net.yanzm.sample.camerax.ui.theme.CameraXComposeSampleTheme

@Composable
fun CaptureScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state = produceState(
        initialValue = null as CamaraProviderAndCapabilities?,
        key1 = context,
        key2 = lifecycleOwner,
        producer = {
            value = getCamaraProviderAndCapabilities(context, lifecycleOwner)
        }
    )

    val cameras = state.value
    if (cameras == null) {
        Text(
            text = "Loading...",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )
    } else {
        val (provider, capabilities) = cameras
        if (capabilities.isEmpty()) {
            Text(
                text = "Error: This device does not have any camera.",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        } else {
            var cameraIndex by remember { mutableStateOf(0) }
            var qualityIndex by remember { mutableStateOf(0) }

            val capability = capabilities[cameraIndex]
            val qualities = capability.qualities
            val quality = capability.qualities[qualityIndex]

            CaptureScreen(
                provider = provider,
                capability = capability,
                quality = quality,
                canSwitchCamera = capabilities.size > 1,
                onClickCameraChange = {
                    cameraIndex = (cameraIndex + 1) % capabilities.size
                    qualityIndex = 0
                },
                onClickQualityChange = { q ->
                    qualityIndex = qualities.indexOf(q).takeIf { it >= 0 } ?: 0
                }
            )
        }
    }
}

private sealed interface BindingState {
    object Initial : BindingState
    object Failed : BindingState
    object Success : BindingState
}

@Composable
private fun CaptureScreen(
    provider: ProcessCameraProvider,
    capability: CameraCapability,
    quality: SupportedQuality,
    canSwitchCamera: Boolean,
    onClickCameraChange: () -> Unit,
    onClickQualityChange: (SupportedQuality) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val (ratio, matchHeightConstraintsFirst) = remember(quality, configuration) {
        quality.getAspectRatio(
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        )
    }

    val videoCapture = remember(quality) {
        VideoCapture.withOutput(
            Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality.value))
                .build()
        )
    }

    var bindingState by remember(provider, quality, lifecycleOwner) {
        mutableStateOf<BindingState>(BindingState.Initial)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(ratio, matchHeightConstraintsFirst),
        factory = { PreviewView(it) },
        update = {
            if (bindingState is BindingState.Initial) {
                bindingState = try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        capability.cameraSelector,
                        videoCapture,
                        androidx.camera.core.Preview.Builder()
                            .setTargetAspectRatio(quality.getAspectRatio())
                            .build()
                            .apply {
                                setSurfaceProvider(it.surfaceProvider)
                            }
                    )
                    BindingState.Success
                } catch (e: Exception) {
                    BindingState.Failed
                }
            }
        }
    )

    when (bindingState) {
        BindingState.Initial -> {
            Text(
                text = "Loading...",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        }
        BindingState.Failed -> {
            Text(
                text = "Error: Use case binding failed.",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        }
        is BindingState.Success -> {
            CaptureController(
                videoCapture = videoCapture,
                quality = quality,
                qualities = capability.qualities,
                canSwitchCamera = canSwitchCamera,
                onClickCameraChange = onClickCameraChange,
                onClickQualityChange = onClickQualityChange,
            )
        }
    }
}

sealed interface ControllerState {
    object Idle : ControllerState
    object WaitRecording : ControllerState
    object Recording : ControllerState
    object WaitPause : ControllerState
    object Paused : ControllerState
    object WaitFinalize : ControllerState
}

@SuppressLint("MissingPermission")
@Composable
private fun CaptureController(
    videoCapture: VideoCapture<Recorder>,
    quality: SupportedQuality,
    qualities: List<SupportedQuality>,
    canSwitchCamera: Boolean,
    onClickCameraChange: () -> Unit = {},
    onClickQualityChange: (SupportedQuality) -> Unit = {},
) {
    val context = LocalContext.current

    var controllerState by remember { mutableStateOf<ControllerState>(ControllerState.Idle) }
    var currentRecording by remember { mutableStateOf<Recording?>(null) }
    var audioChecked by remember { mutableStateOf(false) }
    var captureText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        if (controllerState is ControllerState.Idle) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                AudioCheckbox(
                    checked = audioChecked,
                    onCheckedChange = { audioChecked = it },
                )
                QualityList(
                    qualities = qualities,
                    selected = quality,
                    enabled = currentRecording == null,
                    onClick = onClickQualityChange
                )
            }

            if (canSwitchCamera) {
                CameraSwitchButton(
                    enabled = currentRecording == null,
                    onClick = onClickCameraChange,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                when (controllerState) {
                    ControllerState.Idle -> {
                        StartButton(
                            onClick = {
                                controllerState = ControllerState.WaitRecording

                                currentRecording = videoCapture.output
                                    .prepareRecording(context, createMediaStoreOutput(context))
                                    .apply { if (audioChecked) withAudioEnabled() }
                                    .start(ContextCompat.getMainExecutor(context)) { event ->
                                        captureText = event.text()
                                        when (event) {
                                            is VideoRecordEvent.Status -> {
                                            }
                                            is VideoRecordEvent.Start -> {
                                                controllerState = ControllerState.Recording
                                            }
                                            is VideoRecordEvent.Finalize -> {
                                                controllerState = ControllerState.Idle
                                            }
                                            is VideoRecordEvent.Pause -> {
                                                controllerState = ControllerState.Paused
                                            }
                                            is VideoRecordEvent.Resume -> {
                                                controllerState = ControllerState.Recording
                                            }
                                        }
                                    }
                            }
                        )
                    }
                    ControllerState.Recording -> {
                        PauseButton(
                            onClick = {
                                controllerState = ControllerState.WaitPause
                                currentRecording?.pause()
                            }
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        StopButton(
                            onClick = {
                                controllerState = ControllerState.WaitFinalize
                                currentRecording?.stop()
                                currentRecording = null
                            }
                        )
                    }
                    ControllerState.Paused -> {
                        ResumeButton(
                            onClick = {
                                controllerState = ControllerState.WaitRecording
                                currentRecording?.resume()
                            }
                        )
                    }
                    ControllerState.WaitRecording,
                    ControllerState.WaitPause,
                    ControllerState.WaitFinalize -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(captureText)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StopButton_Preview() {
    CameraXComposeSampleTheme {
        Surface(color = MaterialTheme.colors.background) {
            val quality = SupportedQuality(Quality.UHD)
            CaptureController(
                videoCapture = VideoCapture.withOutput(
                    Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(quality.value))
                        .build()
                ),
                quality = quality,
                qualities = listOf(
                    SupportedQuality(Quality.UHD),
                    SupportedQuality(Quality.FHD),
                    SupportedQuality(Quality.HD),
                    SupportedQuality(Quality.SD),
                ),
                canSwitchCamera = true,
            )
        }
    }
}
