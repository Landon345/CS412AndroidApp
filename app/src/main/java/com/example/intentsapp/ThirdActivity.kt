package com.example.intentsapp

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.intentsapp.ui.theme.IntentsAppTheme
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThirdActivity : ComponentActivity() {

    // The photo on screen, and the file the camera is currently writing to. Each capture gets its
    // own file so that cancelling can't disturb the photo already shown.
    private var photoPath by mutableStateOf<String?>(null)
    private var pendingPhotoPath: String? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val pendingFile = pendingPhotoPath?.let(::File) ?: return@registerForActivityResult
            pendingPhotoPath = null
            if (saved && pendingFile.length() > 0) {
                photoPath?.let { File(it).delete() }
                photoPath = pendingFile.absolutePath
            } else {
                pendingFile.delete()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        photoPath = savedInstanceState?.getString(KEY_PHOTO_PATH)
        pendingPhotoPath = savedInstanceState?.getString(KEY_PENDING_PHOTO_PATH)

        setContent {
            IntentsAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageScreen(
                        modifier = Modifier.padding(innerPadding),
                        photoPath = photoPath,
                        onCaptureClick = ::launchCamera
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PHOTO_PATH, photoPath)
        outState.putString(KEY_PENDING_PHOTO_PATH, pendingPhotoPath)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            photoPath?.let { File(it).delete() }
        }
    }

    private fun launchCamera() {
        val photoFile = try {
            val imagesDir = File(cacheDir, IMAGES_DIR).apply { mkdirs() }
            File.createTempFile("photo_", ".jpg", imagesDir)
        } catch (e: IOException) {
            Toast.makeText(this, R.string.image_file_error, Toast.LENGTH_SHORT).show()
            return
        }

        val photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        pendingPhotoPath = photoFile.absolutePath
        try {
            takePicture.launch(photoUri)
        } catch (e: ActivityNotFoundException) {
            photoFile.delete()
            pendingPhotoPath = null
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val IMAGES_DIR = "images"
        const val KEY_PHOTO_PATH = "photo_path"
        const val KEY_PENDING_PHOTO_PATH = "pending_photo_path"
    }
}

@Composable
fun ImageScreen(
    modifier: Modifier = Modifier,
    photoPath: String?,
    onCaptureClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onCaptureClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.capture_image))
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val bitmap by produceState<Bitmap?>(null, photoPath, width, height) {
                value = photoPath?.let { path ->
                    withContext(Dispatchers.IO) { decodeSampledBitmap(path, width, height) }
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = stringResource(R.string.captured_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Decodes the photo at [path] at roughly the size it is displayed instead of at full resolution,
 * and upright. Returns null if the file is missing or isn't a readable image.
 */
private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
    if (reqWidth <= 0 || reqHeight <= 0) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    // BitmapFactory ignores EXIF orientation, so a sideways photo is measured with the target
    // dimensions swapped and rotated upright after decoding.
    val rotation = exifRotationDegrees(path)
    val sideways = rotation == 90 || rotation == 270
    val targetWidth = if (sideways) reqHeight else reqWidth
    val targetHeight = if (sideways) reqWidth else reqHeight

    // Fit scaling shrinks the photo until its tighter dimension fits, so halve for as long as
    // either dimension would still cover the target.
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= targetWidth ||
        bounds.outHeight / (sampleSize * 2) >= targetHeight
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
    if (rotation == 0) return bitmap

    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun exifRotationDegrees(path: String): Int = try {
    when (
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
} catch (e: IOException) {
    0
}
