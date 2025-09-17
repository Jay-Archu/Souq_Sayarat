package com.personal.sscars24.utils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.personal.sscars24.presentation.ui.login.RegistrationScreen
import kotlinx.coroutines.launch

object NetworkUtils {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun RegistrationScreenPermissionsWrapper(
        onPermissionsResult: (allGranted: Boolean, results: Map<String, Boolean>) -> Unit
    ) {
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Build permission list depending on API level
        val requiredPermissions = remember {
            val perms = mutableListOf<String>()
            // camera needed for camera capture
            perms.add(Manifest.permission.CAMERA)
            // gallery/read media — Android 13 uses READ_MEDIA_IMAGES, older uses READ_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            perms.toTypedArray()
        }

        // Launcher for multiple permissions
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap: Map<String, Boolean> ->
            // permissionsMap maps permission -> granted (Boolean)
            val allGranted = permissionsMap.values.all { it }
            onPermissionsResult(allGranted, permissionsMap)

            if (!allGranted) {
                // Example: show a snackbar explaining why permission is needed
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permissions are required for camera and gallery access.")
                }
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(requiredPermissions)
        }

        // You can show UI for the Registration screen here or call your existing screen
        // For example:
       // RegistrationScreen(snackbarHostState = snackbarHostState)
    }

    fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

}
