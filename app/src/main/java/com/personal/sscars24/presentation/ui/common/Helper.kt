package com.personal.sscars24.presentation.ui.common

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.personal.sscars24.BuildConfig
import com.personal.sscars24.domain.model.CountryCodeResponse
import com.personal.sscars24.utils.DialCodeCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.isNullOrBlank
import kotlin.text.trim

// Simple in-memory cache to avoid repeated network calls
private val isoDialCache = mutableMapOf<String, String?>()

/**
 * Fetch dial code (e.g. "+91") for an ISO (e.g. "IN") from restcountries.
 * Returns dial code string like "+91" or null on failure.
 */
suspend fun getDialCodeForIsoFromRestCountries(iso: String): String? = withContext(Dispatchers.IO) {
    val key = iso.uppercase(Locale.US)
    // return cached if exists (even null cached to avoid retry storms)
    if (isoDialCache.containsKey(key)) return@withContext isoDialCache[key]

    try {
        val endpoint = "https://restcountries.com/v3.1/alpha/${key.lowercase(Locale.US)}"
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/json")
        }

        val code = conn.responseCode
        if (code !in 200..299) {
            conn.disconnect()
            isoDialCache[key] = null
            return@withContext null
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        conn.disconnect()

        // restcountries returns an array
        val arr = JSONArray(sb.toString())
        if (arr.length() == 0) {
            isoDialCache[key] = null
            return@withContext null
        }
        val obj = arr.getJSONObject(0)
        val idd = obj.optJSONObject("idd") ?: run {
            isoDialCache[key] = null
            return@withContext null
        }
        val root = idd.optString("root", "")
        val suffixes = idd.optJSONArray("suffixes")

        if (root.isBlank()) {
            isoDialCache[key] = null
            return@withContext null
        }

        val dial = if (suffixes != null && suffixes.length() > 0) {
            root + (suffixes.optString(0, ""))
        } else root

        // cache and return
        isoDialCache[key] = dial
        return@withContext dial
    } catch (e: Exception) {
        android.util.Log.e("DialCodeFetch", "error fetching dial code for ISO=$iso", e)
        isoDialCache[iso.uppercase(Locale.US)] = null
        return@withContext null
    }
}

/**
 * Resolve current location -> (dialCode, flagUrl) by:
 * 1) Get device location via getLastLocationSuspend (caller should provide fused client)
 * 2) Use Geocoder to obtain country ISO (alpha-2)
 * 3) Get dial code dynamically (cached) using restcountries
 * 4) Find matching item in countryList by dial code (countryList entries have country_code like "+91")
 *
 * Returns Pair(dialCode, flagUrl) or null if unsuccessful.
 */
suspend fun resolveDialAndFlagFromLocationDynamic(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient?,
    countryList: List<com.personal.sscars24.domain.model.CountryCodeResponse>, // adjust import if in different package
    getLastLocationSuspend: suspend (com.google.android.gms.location.FusedLocationProviderClient) -> android.location.Location?
): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        if (fusedLocationClient == null) return@withContext null

        // get location (caller supplies suspend wrapper)
        val loc = try {
            getLastLocationSuspend(fusedLocationClient)
        } catch (e: Exception) {
            null
        }

        if (loc == null) return@withContext null

        // geocoder to get ISO
        val addresses = try {
            Geocoder(context, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)
        } catch (e: Exception) {
            null
        }

        val iso = addresses?.firstOrNull()?.countryCode
        if (iso.isNullOrBlank()) return@withContext null

        // get dial code dynamically (restcountries + cache)
        val dial = getDialCodeForIsoFromRestCountries(iso)
        if (dial.isNullOrBlank()) return@withContext null

        // find match in your API's country list (country_code is dial code like "+91")
        val matched = countryList.find { it.country_code.trim() == dial.trim() }
        Log.e("matched123", matched.toString())
        if (matched != null) {
            val flag = com.personal.sscars24.BuildConfig.BASE_API_URL.trimEnd('/') + matched.country_flag_image
            return@withContext Pair(dial, flag)
        }

        // if not matched, could attempt fallback: try to match by numeric-only dial or other heuristics
        // but for now return null
        return@withContext null
    } catch (e: Exception) {
        android.util.Log.e("ResolveDialFlag", "error resolving dial+flag from location", e)
        return@withContext null
    }
}

fun applyLocaleFallback(
    countryList: List<CountryCodeResponse>,
    onApplied: (String, String) -> Unit
) {
    val iso = Locale.getDefault().country
    val cachedDial = DialCodeCache.get(iso)

    val matched = if (!cachedDial.isNullOrBlank()) {
        countryList.find { it.country_code.trim() == cachedDial.trim() }
    } else {
        countryList.firstOrNull()
    }

    if (matched != null) {
        onApplied(matched.country_code, BuildConfig.BASE_API_URL.trimEnd('/') + matched.country_flag_image)
    } else {
        countryList.firstOrNull()?.let {
            onApplied(it.country_code, BuildConfig.BASE_API_URL.trimEnd('/') + it.country_flag_image)
        } ?: run {
            onApplied(iso, "")
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
suspend fun getLastLocationSuspend(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
): android.location.Location? = suspendCancellableCoroutine { cont ->
    try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { loc -> cont.resume(loc) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    } catch (e: Exception) {
        cont.resumeWithException(e)
    }
}

suspend fun createTempFileFromUri(context: Context, uri: Uri, prefix: String = "upload"): File =
    withContext(Dispatchers.IO) {
        val input: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI")

        val extension = queryFileExtension(context, uri) ?: ".tmp"
        val tempFile = File.createTempFile("${prefix}_${UUID.randomUUID()}", extension, context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            val buffer = ByteArray(8 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            out.flush()
        }
        input.close()
        tempFile
    }
 suspend fun createTempFileFromBitmap(context: Context, bitmap: Bitmap, prefix: String = "camera"): File =
    withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("${prefix}_${UUID.randomUUID()}", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
        }
        tempFile
    }
 fun queryFileExtension(context: Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        when {
            type != null -> {
                when {
                    type.contains("jpeg") || type.contains("jpg") -> ".jpg"
                    type.contains("png") -> ".png"
                    type.contains("pdf") -> ".pdf"
                    else -> "." + (type.substringAfterLast('/'))
                }
            }
            else -> {
                val path = uri.path ?: return null
                val idx = path.lastIndexOf('.')
                if (idx != -1) path.substring(idx) else null
            }
        }
    } catch (e: Exception) {
        null
    }
}

 fun guessMimeType(file: File): String? {
    val name = file.name.lowercase(Locale.getDefault())
    return when {
        name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
        name.endsWith(".png") -> "image/png"
        name.endsWith(".pdf") -> "application/pdf"
        name.endsWith(".txt") -> "text/plain"
        else -> null
    }
}