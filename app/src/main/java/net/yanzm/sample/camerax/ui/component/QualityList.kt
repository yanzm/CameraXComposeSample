package net.yanzm.sample.camerax.ui.component

import android.content.res.Configuration
import androidx.camera.video.Quality
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.yanzm.sample.camerax.data.SupportedQuality
import net.yanzm.sample.camerax.ui.theme.CameraXComposeSampleTheme

@Composable
fun QualityList(
    qualities: List<SupportedQuality>,
    selected: SupportedQuality,
    enabled: Boolean,
    onClick: (SupportedQuality) -> Unit
) {
    Column {
        val qualitySelectorEnabled = qualities.size > 1 && enabled
        qualities.forEach {
            VideoQualityItem(
                text = it.getNameString(),
                isSelected = (it == selected),
                enabled = qualitySelectorEnabled,
                onClick = { onClick(it) }
            )
        }
    }
}

@Composable
private fun VideoQualityItem(
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minWidth = 120.dp)
            .padding(8.dp)
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VideoQualityItem_Preview() {
    CameraXComposeSampleTheme {
        Surface(color = MaterialTheme.colors.background) {
            QualityList(
                qualities = listOf(
                    SupportedQuality(Quality.UHD),
                    SupportedQuality(Quality.FHD),
                    SupportedQuality(Quality.HD),
                    SupportedQuality(Quality.SD),
                ),
                selected = SupportedQuality(Quality.UHD),
                enabled = true,
                onClick = {}
            )
        }
    }
}
