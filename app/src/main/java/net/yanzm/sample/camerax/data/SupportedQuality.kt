package net.yanzm.sample.camerax.data

import androidx.camera.core.AspectRatio
import androidx.camera.video.Quality

data class SupportedQuality(val value: Quality) {

    init {
        require(value in qualities)
    }

    fun getAspectRatio(): Int {
        return when (value) {
            Quality.UHD,
            Quality.FHD,
            Quality.HD -> AspectRatio.RATIO_16_9
            Quality.SD -> AspectRatio.RATIO_4_3
            else -> throw UnsupportedOperationException()
        }
    }

    fun getAspectRatio(portraitMode: Boolean): Pair<Float, Boolean> {
        val ratio = when (getAspectRatio()) {
            AspectRatio.RATIO_16_9 -> Pair(16f, 9f)
            AspectRatio.RATIO_4_3 -> Pair(4f, 3f)
            else -> throw UnsupportedOperationException()
        }

        return if (portraitMode) {
            ratio.second / ratio.first to false
        } else {
            ratio.first / ratio.second to true
        }
    }

    fun getNameString(): String {
        return when (value) {
            Quality.UHD -> "QUALITY_UHD(2160p)"
            Quality.FHD -> "QUALITY_FHD(1080p)"
            Quality.HD -> "QUALITY_HD(720p)"
            Quality.SD -> "QUALITY_SD(480p)"
            else -> throw IllegalArgumentException("Quality $this is NOT supported")
        }
    }

    companion object {
        private val qualities = listOf(
            Quality.UHD,
            Quality.FHD,
            Quality.HD,
            Quality.SD
        )

        fun create(quality: Quality): SupportedQuality? {
            return if (quality in qualities) SupportedQuality(quality) else null
        }
    }
}
