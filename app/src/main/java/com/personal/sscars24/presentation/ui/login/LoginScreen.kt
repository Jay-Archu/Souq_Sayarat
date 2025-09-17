package com.personal.sscars24.presentation.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.personal.sscars24.BuildConfig
import com.personal.sscars24.R
import com.personal.sscars24.domain.model.CountryCodeResponse
import com.personal.sscars24.domain.model.LoginRequest
import com.personal.sscars24.domain.model.LoginResponse
import com.personal.sscars24.domain.model.Resource
import com.personal.sscars24.domain.model.getErrorMessage
import com.personal.sscars24.presentation.ui.common.CircularLoader
import com.personal.sscars24.presentation.ui.common.PermissionSettingsDialog
import com.personal.sscars24.presentation.ui.common.ToastMessage
import com.personal.sscars24.presentation.ui.common.applyLocaleFallback
import com.personal.sscars24.presentation.ui.common.getLastLocationSuspend
import com.personal.sscars24.presentation.ui.common.resolveDialAndFlagFromLocationDynamic
import com.personal.sscars24.presentation.ui.theme.Primary
import com.personal.sscars24.presentation.ui.theme.Secondary
import com.personal.sscars24.presentation.ui.theme.Neutral
import com.personal.sscars24.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun LoginScreen(loginViewmodel: LoginViewModel, onLoginSuccess: (requestId: String?, otp: String?, remainingOtps: Int?,phoneNumber: String?) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    val countryCodesState by loginViewmodel.getCountryInfo.collectAsStateWithLifecycle()
    val loginState by loginViewmodel.getLoginInfo.collectAsStateWithLifecycle(initialValue = Resource.Idle)
    val isLoading = loginState is Resource.Loading
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedFlag by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val blueColor = Primary

    var locationPermissionGranted by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var defaultCountryLoaded by remember { mutableStateOf(false) }
    var hasAskedForPermission by remember { mutableStateOf(false) }
    val isConnected by loginViewmodel.isConnected.collectAsStateWithLifecycle(initialValue = false)
    var firstLaunch by remember { mutableStateOf(true) }
    var pendingResolveByLocation by remember { mutableStateOf(false) }
    var fusedLocationClient: FusedLocationProviderClient? by remember { mutableStateOf(null) }

    LaunchedEffect(isConnected) {
        if (firstLaunch) {
            firstLaunch = false
            return@LaunchedEffect
        }
        if (isConnected && locationPermissionGranted)  {
            loginViewmodel.getCountryCode()
        } else if (!isConnected) {
            ToastMessage(context, "No Internet ❌")
        }
    }

    LaunchedEffect(Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            loginViewmodel.getCountryCode()
            if (countryCodesState is Resource.Success) {
                val codes = (countryCodesState as Resource.Success<List<CountryCodeResponse>>).value
                val result = resolveDialAndFlagFromLocationDynamic(
                    context,
                    fusedLocationClient,
                    codes
                ) { fused ->
                    getLastLocationSuspend(context, fused)
                }
                if (result != null) {
                    selectedCountry = result.first
                    selectedFlag = result.second
                } else {
                    applyLocaleFallback(codes) { code, flag ->
                        selectedCountry = code
                        selectedFlag = flag
                        defaultCountryLoaded = true
                    }
                }
            } else {
                pendingResolveByLocation = true
            }
        } else {
            locationPermissionGranted = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasAskedForPermission = true
        if (isGranted) {
            locationPermissionGranted = true
            loginViewmodel.getCountryCode()
            if (countryCodesState is Resource.Success) {
                val codes = (countryCodesState as Resource.Success<List<CountryCodeResponse>>).value
            } else {
                pendingResolveByLocation = true
            }
        } else {
            locationPermissionGranted = false
            val canShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: true

            if (!canShowRationale) {
                showPermissionSettingsDialog = true
            } else {
                ToastMessage(context, "Location permission denied. Enable the location.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasAskedForPermission) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                hasAskedForPermission = true
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
            }
        }
    }

    LaunchedEffect(countryCodesState) {
        when (val state = countryCodesState) {
            is Resource.Success -> {
                val codes = state.value

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (pendingResolveByLocation) {
                        pendingResolveByLocation = false
                        scope.launch {
                            val result = resolveDialAndFlagFromLocationDynamic(
                                context,
                                fusedLocationClient,
                                codes
                            ) { fused ->
                                getLastLocationSuspend(context, fused)
                            }
                            if (result != null) {
                                selectedCountry = result.first
                                selectedFlag = result.second
                            } else {
                                applyLocaleFallback(codes) { code, flag ->
                                    selectedCountry = code
                                    selectedFlag = flag
                                    defaultCountryLoaded = true
                                }
                            }
                        }
                    } else {
                    }
                } else {
                    if (!defaultCountryLoaded) {
                        applyLocaleFallback(codes) { code, flag ->
                            selectedCountry = code
                            selectedFlag = flag
                            defaultCountryLoaded = true
                        }
                    }
                }
            }

            is Resource.Loading -> {
            }

            is Resource.Failure -> {
                ToastMessage(context, "Failed to load country list")
                if (!defaultCountryLoaded) {
                    defaultCountryLoaded = true
                    val iso = Locale.getDefault().country
                    selectedCountry = iso
                    selectedFlag = ""
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(loginViewmodel) {
        loginViewmodel.getLoginInfo.collect { event ->
            when (event) {
                is Resource.Success -> {
                    val data = event.value
                    if (data.status_code == 200) {
                        ToastMessage(context, data.message)
                        onLoginSuccess(data.request_id, data.otp, data.remaining_otps,phoneNumber)
                    } else {
                        ToastMessage(context, data.message)
                    }
                }

                is Resource.Failure -> {
                    val message =
                        if (event.isNetworkError) "No internet connection" else event.getErrorMessage()
                    ToastMessage(context, message)
                }

                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = blueColor)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(blueColor)
                .padding(top = 60.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.title_name),
                contentDescription = "Title Name"
            )
        }
        Spacer(Modifier.height(15.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome),
                        fontSize = 16.sp,
                        color = blueColor
                    )
                    Text(
                        text = stringResource(id = R.string.sayarat_text),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = stringResource(id = R.string.phone_number),
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(15.dp))
                    Text(
                        text = stringResource(id = R.string.phone_number1),
                        fontSize = 12.sp,
                        color = Secondary,
                        fontWeight = FontWeight.Normal,
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(0.37f)) {
                            when (val state = countryCodesState) {
                                is Resource.Success -> {
                                    val codes = state.value
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextField(
                                            value = selectedCountry,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = expanded
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = TextFieldDefaults.textFieldColors(
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                errorIndicatorColor = Color.Transparent,
                                                cursorColor = Primary
                                            ),
                                            leadingIcon = {
                                                if (selectedFlag.isNotEmpty()) {
                                                    GlideImage(
                                                        model = selectedFlag,
                                                        contentDescription = "Selected Flag",
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .padding(start = 0.dp, end = 0.dp)
                                                    )
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 11.sp,
                                                color = Color.Black
                                            ),
                                            singleLine = true,
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }) {
                                            codes.forEach { code ->
                                                DropdownMenuItem(onClick = {
                                                    selectedCountry = code.country_code
                                                    selectedFlag =
                                                        BuildConfig.BASE_API_URL.trimEnd('/') + code.country_flag_image
                                                    expanded = false
                                                }) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        GlideImage(
                                                            model = BuildConfig.BASE_API_URL.trimEnd(
                                                                '/'
                                                            ) + code.country_flag_image,
                                                            contentDescription = "Country Flag",
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .padding(end = 8.dp)
                                                        )
                                                        Text(
                                                            text = code.country_code,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                is Resource.Loading -> {
                                    CircularLoader()
                                }

                                is Resource.Failure -> {
                                    TextField(
                                        value = "",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent,
                                            cursorColor = Primary
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 11.sp,
                                            color = Color.Black
                                        ),
                                        singleLine = true,
                                    )
                                }

                                else -> {
                                    TextField(
                                        value = "",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent,
                                            cursorColor = Primary
                                        ),
                                        textStyle = TextStyle(
                                            fontSize = 11.sp,
                                            color = Color.Black
                                        ),
                                        singleLine = true,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        TextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            placeholder = { Text(" phone number", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(0.63f)
                                .border(
                                    width = 2.dp,
                                    color = if (phoneNumber.isNotEmpty()) Primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                cursorColor = Primary
                            ),
                            textStyle = TextStyle(fontSize = 14.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val request = LoginRequest(
                            phoneNumber,
                            "RN6s6IEGSpRAkrz5B09izLkdje3s-_CufmeRVMbDFVLemdKsxZQxbT8jDFrrh0wJ8WS7mGkF3Ywyd2TG7cy8-HQJeZtO7XBg9bPVNwiVr-fbsiTViU_CIAXUt9zckBvM9hZsTRucJzAioNLke6aplIrMmra4Ozrul6N2wv8vXzO1kdWBeVROJF6xf9l0P11bSyYEFV3RNdL4swCjBFyTYegnl5BjACzNlakl5IQpDgpCMUG1ODzWESBwTx4D-coF1HSELzIS7wWqReQoV18Y92C1HS8jcWrC3Iy4nkPBFj5aN_4pAZo93O5m4C6a8HWNYV_UFavkk7SClC4dP0jGArchsUTo0AEypfQmyPO_acUJpRic-AFJH18zYkv_tWgjEE1aaKceOjO5ubfzej2fCi1n6kph2IqT49AtHvFKK4FGtkb8PSN20Ln8efQToMuLpSWUoUOaEuHaFenthKx23IEllBgmy_Rbo-33Nl9G5Bbtyz3IXLxDzqmgWH1lC35xraIouPkysKi53JmmiAqzWmKIO4PH_AnC4r2P7GCcjOP60xcZh0YBmU2dWIwd1qSED1uHwg5XOc8R5ua6qu5RoU8yaxfj2QKTqNLviHwSCy4zRJXUb5eDbOs_r1kp_2nUXughgJUtvMNA4RZFHsy_MM8v_5H_pDm1xAlJrVHwO-jqab4WtgoacK6Io_p_lZjIkoAxzNy5SG8n_faIq27Tai-qwQ0frbs3MnIJc_ueD80-Of2x_8BjRyEVhOopFsMZnxz3eCGm822rB4B-Ag0X_J29oI5VLlBZZlC5DFqq-b7yv98RFCvAGcKXbzvAoHzpuvU7Kx8dMyvqSgrX23kqLVUuUKqBDez5xLGKUxHg8y89n223HW33l97qtbQIq2hXJzNSddhb-jGpJEjtaSR2S1IC9fNzH5M1sCzLjhFQHV6F-jGioKW5vIhXD6w3sUKgusiGGX7QH465064GE6qS0BXaGY_8B22Q"
                        )
                        loginViewmodel.postLogin(request)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = phoneNumber.isNotEmpty() && selectedCountry.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (phoneNumber.isNotEmpty() && selectedCountry.isNotEmpty()) Primary else Color.LightGray.copy(
                            alpha = 0.2f
                        ),
                        contentColor = if (phoneNumber.isNotEmpty() && selectedCountry.isNotEmpty()) Color.White else Neutral
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Please wait")
                    } else {
                        Text("Continue")
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Continue as guest", color = blueColor)
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showPermissionSettingsDialog) {
        PermissionSettingsDialog(
            onDismiss = { showPermissionSettingsDialog = false },
            onSettingsOpened = {
                showPermissionSettingsDialog = false
            },
            onPermissionGranted = {
                locationPermissionGranted = true
                loginViewmodel.getCountryCode()
            }
        )
    }

}
