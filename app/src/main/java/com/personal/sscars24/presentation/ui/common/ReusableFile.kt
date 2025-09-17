package com.personal.sscars24.presentation.ui.common

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.personal.sscars24.presentation.ui.theme.Primary
import java.time.LocalDate
import java.util.Calendar

@Composable
fun StatusBarFile() {
    val systemUiController = rememberSystemUiController()
    val tealColor = Primary

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(color = tealColor)
    }
}


fun ToastMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun CircularLoader(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = Primary
        )
    }
}

@Composable
fun PermissionSettingsDialog(
    onDismiss: () -> Unit,
    onSettingsOpened: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onPermissionGranted()
        } else {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Location") },
        text = { Text("Location permission is required to auto-detect your country. Please enable it in App Settings.") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                launcher.launch(intent)
            }) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun showDatePickerDialog(
    context: Context,
    initialDate: LocalDate?,
    minDate: LocalDate = LocalDate.of(1900, 1, 1),
    maxDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    val init = initialDate ?: maxDate
    val initYear = init.year
    val initMonth = init.monthValue - 1
    val initDay = init.dayOfMonth

    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initYear,
        initMonth,
        initDay
    )

    val maxCal = Calendar.getInstance().apply {
        set(maxDate.year, maxDate.monthValue - 1, maxDate.dayOfMonth, 23, 59, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val minCal = Calendar.getInstance().apply {
        set(minDate.year, minDate.monthValue - 1, minDate.dayOfMonth, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    dialog.datePicker.maxDate = maxCal.timeInMillis
    dialog.datePicker.minDate = minCal.timeInMillis

    dialog.show()
}

