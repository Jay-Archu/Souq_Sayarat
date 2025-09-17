package com.personal.sscars24.presentation.ui.login

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.sscars24.R
import com.personal.sscars24.data.local.UserPreferences
import com.personal.sscars24.domain.model.OtpRequest
import com.personal.sscars24.domain.model.ResendOtpRequest
import com.personal.sscars24.domain.model.Resource
import com.personal.sscars24.domain.model.getErrorMessage
import com.personal.sscars24.presentation.ui.common.ToastMessage
import com.personal.sscars24.presentation.ui.theme.Neutral
import com.personal.sscars24.presentation.ui.theme.Primary
import com.personal.sscars24.presentation.ui.theme.Secondary1
import com.personal.sscars24.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpScreen(
    loginViewmodel: LoginViewModel,
    phoneNumber: String? = null,
    requestId: String? = null,
    initialOtp: String? = null,
    remainingOtps: Int = 0,
    onOtpSuccess: () -> Unit
) {
    val blueColor = Primary
    val context = LocalContext.current
    val digitsCount: Int = 4

    val otpState by loginViewmodel.verifyOtpInfo.collectAsStateWithLifecycle(initialValue = Resource.Idle)
    val isLoading = otpState is Resource.Loading

    var currentRequestId by rememberSaveable { mutableStateOf(requestId) }
    val digits = remember { List(digitsCount) { mutableStateOf("") } }
    val focusRequesters = remember { List(digitsCount) { FocusRequester() } }
    val interactionSources = remember { List(digitsCount) { MutableInteractionSource() } }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    fun currentOtp() = digits.joinToString("") { it.value }
    val otp by remember { derivedStateOf { currentOtp() } }
    val isButtonEnabled by remember { derivedStateOf { otp.length == digitsCount } }
    val lastFilledIndex by remember { derivedStateOf { digits.indexOfLast { it.value.isNotEmpty() } } }

    var remainingSeconds by rememberSaveable { mutableStateOf(0) }
    var inFlight by rememberSaveable { mutableStateOf(false) }
    var lastHandledResendEvent by rememberSaveable { mutableStateOf(0L) }

    /*fun prefillOtp(otpStr: String?) {
        if (otpStr.isNullOrBlank()) return
        val trimmed = otpStr.filter { it.isDigit() }.take(digitsCount)
        trimmed.forEachIndexed { idx, ch ->
            digits.getOrNull(idx)?.value = ch.toString()
        }
    }

    LaunchedEffect(initialOtp) {
        prefillOtp(initialOtp)
    }*/


    @SuppressLint("DefaultLocale")
    fun formatSeconds(seconds: Int): String {
        val mm = seconds / 60
        val ss = seconds % 60
        return String.format("%02d:%02d", mm, ss)
    }

    fun startTimer(seconds: Int = 60) {
        if (remainingSeconds > 0) return
        remainingSeconds = seconds
        scope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (remainingSeconds == 0) {
            startTimer(60)
        }
    }

    LaunchedEffect(loginViewmodel) {
        loginViewmodel.resendOtpInfo.collect { event ->
            when (event) {
                is Resource.Success -> {
                    val data = event.value
                    val eventId = System.currentTimeMillis()
                    if (data.status_code == 200 && eventId != lastHandledResendEvent) {
                        lastHandledResendEvent = eventId
                        currentRequestId = data.request_id
                        ToastMessage(context, data.otp)
                        /*data.otp?.let { otpFromServer ->
                            prefillOtp(otpFromServer)
                        }*/
                        startTimer(60)
                    } else {
                        ToastMessage(context, data.message)
                    }
                    inFlight = false
                }

                is Resource.Failure -> {
                    val message =
                        if (event.isNetworkError) "No internet connection" else event.getErrorMessage()
                    ToastMessage(context, message)
                    inFlight = false
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(loginViewmodel) {
        loginViewmodel.verifyOtpInfo.collect { event ->
            when (event) {
                is Resource.Success -> {
                    val data = event.value
                    if (data.status_code == 200) {
                        loginViewmodel.saveKey(UserPreferences.Token, data.access_token)
                        onOtpSuccess()
                        ToastMessage(context, data.message)
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.verify),
                        fontSize = 16.sp,
                        color = blueColor
                    )
                    Text(
                        text = stringResource(id = R.string.phone),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = stringResource(id = R.string.otp_number),
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(5.dp))
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                ) {
                    for (i in 0 until digitsCount) {
                        val state = digits[i]
                        val isFilled = state.value.isNotEmpty()
                        val isLastFilled = i == lastFilledIndex
                        val backgroundColor = if (isLastFilled && isFilled) Primary else Secondary1
                        val isFocused = interactionSources[i].collectIsFocusedAsState().value
                        val borderColor = when {
                            isFilled -> Primary
                            isFocused -> Primary
                            else -> Secondary1
                        }
                        val textColor = if (isLastFilled && isFilled) Color.White else Color.Black
                        BasicTextField(
                            value = state.value,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                if (filtered == state.value) return@BasicTextField
                                if (filtered.length > 1) {
                                    val pasted = filtered.take(digitsCount)
                                    pasted.forEachIndexed { index, ch ->
                                        digits.getOrNull(index)?.value = ch.toString()
                                    }
                                    val lastIndex = (pasted.length - 1).coerceAtMost(digitsCount - 1)
                                    scope.launch {
                                        delay(50)
                                        focusRequesters.getOrNull(lastIndex)?.requestFocus()
                                        keyboard?.hide()
                                    }
                                } else {
                                    state.value = filtered
                                    if (filtered.isNotEmpty()) {
                                        if (i < digitsCount - 1) {
                                            scope.launch {
                                                delay(50)
                                                focusRequesters[i + 1].requestFocus()
                                            }
                                        } else {
                                            scope.launch {
                                                delay(50)
                                                keyboard?.hide()
                                            }
                                        }
                                    } else {
                                        if (i > 0) {
                                            scope.launch {
                                                delay(30)
                                                focusRequesters[i - 1].requestFocus()
                                            }
                                        }
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = textColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (i == digitsCount - 1) ImeAction.Done else ImeAction.Next
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .focusRequester(focusRequesters[i]),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = backgroundColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .then(
                                            if (borderColor != Color.Transparent)
                                                Modifier.border(
                                                    2.dp,
                                                    borderColor,
                                                    RoundedCornerShape(12.dp)
                                                )
                                            else Modifier
                                        )
                                        .size(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.value.isEmpty()) {
                                        Text(text = "", fontSize = 24.sp, color = Color.Gray)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val request = OtpRequest(
                            otp,
                            currentRequestId ?: "",
                            remainingOtps
                        )
                        loginViewmodel.otpVerify(request)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isButtonEnabled && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonEnabled) Primary else Color.LightGray.copy(alpha = 0.2f),
                        contentColor = if (isButtonEnabled) Color.White else Neutral
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

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        if (inFlight || remainingSeconds > 0) return@TextButton
                        if (phoneNumber.isNullOrBlank()) {
                            ToastMessage(context, "Missing phone number")
                            return@TextButton
                        }
                        inFlight = true
                        val request = ResendOtpRequest(phoneNumber)
                        loginViewmodel.resendOtp(request)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !inFlight && remainingSeconds == 0
                ) {
                    when {
                        inFlight -> Text("Resending...", color = blueColor)
                        remainingSeconds > 0 -> Text("Resend - ${formatSeconds(remainingSeconds)}", color = blueColor)
                        else -> Text("Resend", color = blueColor)
                    }
                }
            }
        }
    }
}