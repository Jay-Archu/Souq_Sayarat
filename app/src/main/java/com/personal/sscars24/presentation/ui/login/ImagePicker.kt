@file:OptIn(ExperimentalGlideComposeApi::class)

package com.personal.sscars24.presentation.ui.login


import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.FileProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.personal.sscars24.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * ImagePickerBox
 *
 * @param previewUri - if non-null, displayed as avatar (replaces placeholder)
 * @param sizeDp - avatar size in dp (default 100)
 * @param placeholderRes - placeholder drawable resource id
 * @param authority - FileProvider authority. If null, defaults to "${context.packageName}.fileprovider"
 * @param onImagePicked - callback invoked with picked Uri? (null if cancelled)
 */
@Composable
fun ImagePickerBox(
    modifier: Modifier = Modifier,
    previewUri: Uri? = null,
    placeholderRes: Int = R.drawable.image,
    sizeDp: Int = 120,
    authority: String? = null,
    onImagePicked: (uri: Uri?) -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val resolvedAuthority = authority ?: "${context.packageName}.fileprovider"

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            onImagePicked(cameraImageUri)
        } else {
            onImagePicked(null)
            cameraImageUri = null
        }
        showDialog = false
    }


    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImagePicked(uri)
        showDialog = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageFileUriOrNull(context, resolvedAuthority)
            if (uri != null) {
                cameraImageUri = uri
                takePictureLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Failed to create file for camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take photo", Toast.LENGTH_SHORT).show()
        }
        showDialog = false
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (previewUri != null) {
            GlideImage(
                model = previewUri,
                contentDescription = "picked image",
                modifier = Modifier
                    .size(sizeDp.dp)
                    .clip(CircleShape)
                    .clickable { showDialog = true },
                contentScale = ContentScale.Crop,
            )
        } else {
            GlideImage(
                model = placeholderRes,
                contentDescription = "placeholder",
                modifier = Modifier
                    .size(sizeDp.dp)
                    .clickable { showDialog = true },
                contentScale = ContentScale.Crop
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Option") },
            text = {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {

                    Text(
                        text = "Camera",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                val perm = Manifest.permission.CAMERA
                                val status = ContextCompat.checkSelfPermission(context, perm)
                                if (status == PermissionChecker.PERMISSION_GRANTED) {
                                    val uri = createImageFileUriOrNull(context, resolvedAuthority)
                                    if (uri != null) {
                                        cameraImageUri = uri
                                        takePictureLauncher.launch(uri)
                                    } else {
                                        Toast.makeText(context, "Failed to create file for camera", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(12.dp)
                    )

                    Text(
                        text = "Gallery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                getContentLauncher.launch("image/*")
                            }
                            .padding(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            dismissButton = {}
        )
    }
}

private fun grantPersistableUriPermissionIfNeeded(context: Context, uri: Uri) {
    try {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    } catch (_: Exception) {
    }
}
private fun createImageFileUriOrNull(context: android.content.Context, authority: String): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        val file = File.createTempFile(imageFileName, ".jpg", storageDir)
        file.apply { createNewFile(); deleteOnExit() }
        FileProvider.getUriForFile(context, authority, file)
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            name = it.getString(nameIndex)
        }
    }
    if (name.isNullOrBlank()) {
        name = uri.lastPathSegment ?: "file"
    }
    return name
}

