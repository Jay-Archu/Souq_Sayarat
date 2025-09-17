package com.personal.sscars24.presentation.ui.login

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.personal.sscars24.BuildConfig
import com.personal.sscars24.R
import com.personal.sscars24.domain.model.Resource
import com.personal.sscars24.domain.model.getErrorMessage
import com.personal.sscars24.presentation.ui.common.ToastMessage
import com.personal.sscars24.presentation.ui.common.showDatePickerDialog
import com.personal.sscars24.presentation.ui.theme.Primary
import com.personal.sscars24.presentation.ui.theme.Secondary
import com.personal.sscars24.presentation.ui.theme.Secondary1
import com.personal.sscars24.presentation.viewmodel.LoginViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.sscars24.domain.model.RegisterRequest

@SuppressLint("UseKtx")
@OptIn(ExperimentalGlideComposeApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen(
    loginViewmodel: LoginViewModel,
    phoneNumber1: String?,
    onRegisterSuccess: () -> Unit
) {
    val blueColor = Primary
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var companyRegNum by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }
    var companyNameError by remember { mutableStateOf(false) }
    var ownerNameError by remember { mutableStateOf(false) }
    var companyAddressError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var companyRegError by remember { mutableStateOf(false) }
    var uploadDoc by remember { mutableStateOf("") }
    var uploadDocError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val textValue by remember { derivedStateOf { selectedDate?.format(formatter) ?: "" } }
    var isDealer by remember { mutableStateOf<Boolean?>(false) }
    var uploadDocFile by remember { mutableStateOf<String>("") }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var previewUri1 by remember { mutableStateOf<Uri?>(null) }
    val registerState by loginViewmodel.registerInfo.collectAsStateWithLifecycle(initialValue = Resource.Idle)
    val isLoading = registerState is Resource.Loading
    LaunchedEffect(loginViewmodel) {
        loginViewmodel.uploadInfo.collect { event ->
            when (event) {
                is Resource.Success -> {
                    val data = event.value
                    if (data != null && data.status_code == 200) {
                        val returnedUrl = data.attachment_url
                        val finalUrl = if (returnedUrl.startsWith("http")) {
                            returnedUrl
                        } else {
                            BuildConfig.BASE_API_URL + returnedUrl
                        }
                        uploadDoc = finalUrl
                        previewUri = finalUrl.toUri()
                        ToastMessage(context, data.message)
                    } else {
                        ToastMessage(
                            context,
                            data.message
                        )
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

    LaunchedEffect(loginViewmodel) {
        loginViewmodel.registerInfo.collect { event ->
            when (event) {
                is Resource.Success -> {
                    val data = event.value
                    if (data != null && data.status_code == 200) {
                        ToastMessage(context, data.message)
                        onRegisterSuccess()
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
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.new_here),
                        fontSize = 16.sp,
                        color = blueColor
                    )
                    Text(
                        text = stringResource(id = R.string.create_new_account),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                    Spacer(Modifier.height(25.dp))
                    ImagePickerBox(
                        previewUri = previewUri,
                        placeholderRes = R.drawable.image,
                        sizeDp = 120,
                        authority = "${context.packageName}.fileprovider",
                        onImagePicked = { uri ->
                            if (uri != null) {
                                previewUri = uri
                                uploadDoc = uri.toString()
                                try {
                                    loginViewmodel.uploadAttachment(uri = uri,)
                                } catch (e: Exception) {
                                    Log.e("RegistrationScreen", "upload start failed: ${e.message}")
                                }
                            } else {
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "First Name*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = Secondary
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                        name = newValue
                                    }
                                    if (newValue.isNotEmpty()) {
                                        showError = false
                                    }
                                },
                                placeholder = { Text("first name") },
                                singleLine = true,
                                isError = showError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Secondary1, shape = RoundedCornerShape(15.dp)),
                                shape = RoundedCornerShape(15.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = if (showError) MaterialTheme.colorScheme.error else Secondary1,
                                    focusedContainerColor = Secondary1,
                                    unfocusedContainerColor = Secondary1
                                )
                            )

                            if (showError) {
                                Text(
                                    text = "First name is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Last Name*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = Secondary
                            )

                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                        lastName = newValue
                                    }
                                    if (newValue.isNotEmpty()) {
                                        lastNameError = false
                                    }
                                },
                                placeholder = { Text(" last name") },
                                singleLine = true,
                                isError = lastNameError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Secondary1, shape = RoundedCornerShape(15.dp)),
                                shape = RoundedCornerShape(15.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = if (lastNameError) MaterialTheme.colorScheme.error else Secondary1,
                                    focusedContainerColor = Secondary1,
                                    unfocusedContainerColor = Secondary1
                                )
                            )

                            if (lastNameError) {
                                Text(
                                    text = "Last name is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Email",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = Secondary
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { newValue ->
                                    email = newValue
                                    emailError = !emailPattern.matches(newValue)
                                },
                                placeholder = { Text(" email") },
                                singleLine = true,
                                isError = emailError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Secondary1, shape = RoundedCornerShape(15.dp)),
                                shape = RoundedCornerShape(15.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = if (emailError) MaterialTheme.colorScheme.error else Secondary1,
                                    focusedBorderColor = if (emailError) MaterialTheme.colorScheme.error else Secondary1,
                                    focusedContainerColor = Secondary1,
                                    unfocusedContainerColor = Secondary1
                                )
                            )

                            if (emailError) {
                                Text(
                                    text = "Invalid email address",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Date of Birth*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = Secondary
                            )

                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { },
                                readOnly = true,
                                placeholder = { Text("DD/MM/YYYY") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Pick date",
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clickable {
                                                showDatePickerDialog(
                                                    context = context,
                                                    initialDate = selectedDate,
                                                    minDate = LocalDate.of(1900, 1, 1),
                                                    maxDate = LocalDate.now(),
                                                    onDateSelected = { picked ->
                                                        selectedDate = picked
                                                        dateError = false
                                                    }
                                                )
                                            }
                                    )
                                },
                                shape = RoundedCornerShape(15.dp),
                                isError = dateError,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Secondary1,
                                    unfocusedContainerColor = Secondary1,
                                    unfocusedBorderColor = Secondary1,
                                    focusedBorderColor = Secondary1
                                ),
                                keyboardOptions = KeyboardOptions.Default
                            )

                            if (dateError) {
                                Text(
                                    text = "Date of birth is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Are You A Dealer?*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = Secondary
                            )

                            Row(
                                modifier = Modifier
                                    .selectableGroup()
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .selectable(
                                            selected = isDealer == true,
                                            onClick = { isDealer = true },
                                            role = Role.RadioButton
                                        )
                                        .padding(end = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = isDealer == true,
                                        onClick = {
                                            isDealer = true
                                            companyNameError = false
                                            ownerNameError = false
                                            companyAddressError = false
                                            phoneNumberError = false
                                            companyRegError = false
                                            uploadDocError = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Primary,
                                        )
                                    )
                                    Text(
                                        text = "Yes",
                                        modifier = Modifier.padding(start = 2.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDealer == true) Primary else Color.Black,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .selectable(
                                            selected = isDealer == false,
                                            onClick = { isDealer = false },
                                            role = Role.RadioButton
                                        )
                                ) {
                                    RadioButton(
                                        selected = isDealer == false,
                                        onClick = {
                                            isDealer = false
                                            companyName = ""
                                            ownerName = ""
                                            companyAddress = ""
                                            phoneNumber = ""
                                            companyRegNum = ""
                                            uploadDoc = ""
                                            companyNameError = false
                                            ownerNameError = false
                                            companyAddressError = false
                                            phoneNumberError = false
                                            companyRegError = false
                                            uploadDocError = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Primary,
                                        )
                                    )
                                    Text(
                                        text = "No",
                                        modifier = Modifier.padding(start = 2.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDealer == false) Primary else Color.Black,
                                    )
                                }
                            }
                        }

                        if (isDealer == true) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Company Name*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = companyName,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                                companyName = newValue
                                            }
                                            if (newValue.isNotEmpty()) {
                                                companyNameError = false
                                            }
                                        },
                                        placeholder = { Text(" company name") },
                                        singleLine = true,
                                        isError = companyNameError,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (companyNameError) MaterialTheme.colorScheme.error else Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )

                                    if (companyNameError) {
                                        Text(
                                            text = "Company name is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Owner's Name*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = ownerName,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                                ownerName = newValue
                                            }
                                            if (newValue.isNotEmpty()) {
                                                ownerNameError = false
                                            }
                                        },
                                        placeholder = { Text(" owner name") },
                                        singleLine = true,
                                        isError = ownerNameError,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (ownerNameError) MaterialTheme.colorScheme.error else Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )

                                    if (ownerNameError) {
                                        Text(
                                            text = "Owner name is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Company Address*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = companyAddress,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                                companyAddress = newValue
                                            }
                                            if (newValue.isNotEmpty()) {
                                                companyAddressError = false
                                            }
                                        },
                                        placeholder = { Text(" company address") },
                                        singleLine = true,
                                        isError = companyAddressError,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (companyAddressError) MaterialTheme.colorScheme.error else Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )

                                    if (companyAddressError) {
                                        Text(
                                            text = "Company address is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Phone Number*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 15 && newValue.all { it.isDigit() || it.isWhitespace() }) {
                                                phoneNumber = newValue
                                            }
                                            if (newValue.isNotEmpty()) {
                                                phoneNumberError = false
                                            }
                                        },
                                        placeholder = { Text(" phone number") },
                                        singleLine = true,
                                        isError = phoneNumberError,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (phoneNumberError) MaterialTheme.colorScheme.error else Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )

                                    if (phoneNumberError) {
                                        Text(
                                            text = "Phone number is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Company Registration Number*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = companyRegNum,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
                                                companyRegNum = newValue
                                            }
                                            if (newValue.isNotEmpty()) {
                                                companyRegError = false
                                            }
                                        },
                                        placeholder = { Text("company registration number") },
                                        singleLine = true,
                                        isError = companyRegError,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (companyRegError) MaterialTheme.colorScheme.error else Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )

                                    if (companyRegError) {
                                        Text(
                                            text = "Company Registration number is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Facebook Page (Optional)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = facebook,
                                        onValueChange = { newValue ->
//                                if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
//                                    companyRegNum = newValue
//                                }
                                            facebook = newValue
                                        },
                                        placeholder = { Text("facebook page url") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Instagram Company Profile (Optional)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = instagram,
                                        onValueChange = { newValue ->
//                                if (newValue.length <= 50 && newValue.all { it.isLetter() || it.isWhitespace() }) {
//                                    companyRegNum = newValue
//                                }
                                            instagram = newValue
                                        },
                                        placeholder = { Text("instagram page url") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Secondary1,
                                                shape = RoundedCornerShape(15.dp)
                                            ),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Secondary1,
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1
                                        )
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Upload Documents*",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        color = Secondary
                                    )

                                    OutlinedTextField(
                                        value = uploadDocFile,
                                        onValueChange = { },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                //showPicker = true
                                            },
                                        readOnly = true,
                                        placeholder = { Text("Documents") },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Pick date",
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .clickable {
                                                        //showPicker = true
                                                    }
                                            )
                                        },
                                        shape = RoundedCornerShape(15.dp),
                                        isError = uploadDocError,
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Secondary1,
                                            unfocusedContainerColor = Secondary1,
                                            unfocusedBorderColor = Secondary1,
                                            focusedBorderColor = Secondary1
                                        ),
                                        keyboardOptions = KeyboardOptions.Default
                                    )

                                   /* if (uploadDocError) {
                                        Text(
                                            text = "Upload doc is required",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                        )
                                    }*/
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "WhatsApp",style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                color = Secondary)
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Secondary1
                            )
                            )
                        }

                        Button(
                            onClick = {
                                var valid = true
                                if (name.isBlank()) {
                                    showError = true
                                    valid = false
                                }
                                if (lastName.isBlank()) {
                                    lastNameError = true
                                    valid = false
                                }
                                if (email.isBlank() || !emailPattern.matches(email)) {
                                    emailError = true
                                    valid = false
                                }
                                if (selectedDate == null) {
                                    dateError = true
                                    valid = false
                                }
                                if (isDealer == true) {
                                    if (companyName.isBlank()) {
                                        companyNameError = true
                                        valid = false
                                    }
                                    if (ownerName.isBlank()) {
                                        ownerNameError = true
                                        valid = false
                                    }
                                    if (companyAddress.isBlank()) {
                                        companyAddressError = true
                                        valid = false
                                    }
                                    if (phoneNumber.isBlank()) {
                                        phoneNumberError = true
                                        valid = false
                                    }
                                    if (companyRegNum.isBlank()) {
                                        companyRegError = true
                                        valid = false
                                    }
                                    /*if (uploadDocFile.isNotEmpty()) {
                                        uploadDocError = true
                                        valid = false
                                    }*/
                                }

                                if (valid) {
                                    val userType = if (isDealer == true) "Dealer" else "Individual"
                                    val userType1 = isDealer == true
                                    val enabled = if (enabled) "1" else "0"
                                    val request = RegisterRequest(name,lastName,email,textValue,userType,companyName,ownerName,companyAddress,phoneNumber,companyRegNum,facebook,instagram,uploadDoc,phoneNumber1!!,userType1,enabled,uploadDocFile)
                                     loginViewmodel.register(request)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color.White
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
                                Text("Create Account")
                            }
                        }
                    }
                }
            }
        }
    }
}
