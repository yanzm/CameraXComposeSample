package net.yanzm.sample.camerax.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.yanzm.sample.camerax.R
import net.yanzm.sample.camerax.ui.theme.CameraXComposeSampleTheme

@Composable
fun ResumeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.ic_resume),
            contentDescription = stringResource(R.string.resume),
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ResumeButton_Preview() {
    CameraXComposeSampleTheme {
        Surface(color = MaterialTheme.colors.background) {
            ResumeButton(onClick = {})
        }
    }
}
