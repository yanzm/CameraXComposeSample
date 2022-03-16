package net.yanzm.sample.camerax.ui

import android.content.Context
import android.provider.MediaStore
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.contentValuesOf
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

fun createMediaStoreOutput(context: Context): MediaStoreOutputOptions {
    val name = "CameraX-recording-" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis()) + ".mp4"

    val contentValues = contentValuesOf(
        MediaStore.Video.Media.DISPLAY_NAME to name
    )

    return MediaStoreOutputOptions
        .Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        .setContentValues(contentValues)
        .build()
}

fun VideoRecordEvent.text(): String {
    val stats = recordingStats
    val size = stats.numBytesRecorded / 1000
    val time = TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
    val state = when (this) {
        is VideoRecordEvent.Status -> "Status"
        is VideoRecordEvent.Start -> "Started"
        is VideoRecordEvent.Finalize -> "Finalized"
        is VideoRecordEvent.Pause -> "Paused"
        is VideoRecordEvent.Resume -> "Resumed"
        else -> throw IllegalArgumentException("Unknown VideoRecordEvent: $this")
    }
    return "${state}: recorded ${size}KB, in ${time}second" +
        if (this is VideoRecordEvent.Finalize) {
            "\nFile saved to: ${this.outputResults.outputUri}"
        } else {
            ""
        }
}
