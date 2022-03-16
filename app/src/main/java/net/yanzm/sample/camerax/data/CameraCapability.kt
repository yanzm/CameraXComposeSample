package net.yanzm.sample.camerax.data

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.QualitySelector
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner

data class CamaraProviderAndCapabilities(
    val provider: ProcessCameraProvider,
    val capabilities: List<CameraCapability>
)

data class CameraCapability(
    val cameraSelector: CameraSelector,
    val qualities: List<SupportedQuality>
) {
    init {
        require(qualities.isNotEmpty())
    }
}

private val cameraSelectors = arrayOf(
    CameraSelector.DEFAULT_BACK_CAMERA,
    CameraSelector.DEFAULT_FRONT_CAMERA
)

suspend fun getCamaraProviderAndCapabilities(
    context: Context,
    lifecycleOwner: LifecycleOwner
): CamaraProviderAndCapabilities {

    val provider = ProcessCameraProvider.getInstance(context).await()

    provider.unbindAll()

    val capabilities = cameraSelectors.mapNotNull { cameraSelector ->
        try {
            if (provider.hasCamera(cameraSelector)) {
                val camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector)
                val qualities = QualitySelector
                    .getSupportedQualities(camera.cameraInfo)
                    .mapNotNull { SupportedQuality.create(it) }

                if (qualities.isNotEmpty()) {
                    CameraCapability(cameraSelector, qualities)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (exc: Exception) {
            println("Camera Face $cameraSelector is not supported")
            null
        }
    }

    return CamaraProviderAndCapabilities(provider, capabilities)
}
