package com.personal.sscars24.presentation.ui.splash

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.personal.sscars24.R
import com.personal.sscars24.presentation.ui.theme.Primary
import com.personal.sscars24.presentation.viewmodel.LoginViewModel
import com.personal.sscars24.utils.NetworkUtils.RegistrationScreenPermissionsWrapper
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onContinueClicked: () -> Unit, loginViewmodel: LoginViewModel) {

    val permissionsGranted by loginViewmodel.permissionsGranted.collectAsState()
    RegistrationScreenPermissionsWrapper { allGranted, perms ->

        if (allGranted) {
            loginViewmodel.setPermissionsGranted(true)
        } else {
            val cameraGranted = perms[Manifest.permission.CAMERA] == true
            val galleryGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms[Manifest.permission.READ_MEDIA_IMAGES] == true
            } else {
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }

            loginViewmodel.setPermissionsGranted(cameraGranted && galleryGranted)

            if (!cameraGranted) {
                println("⚠️ Camera permission denied")
            }
            if (!galleryGranted) {
                println("⚠️ Gallery permission denied")
            }
        }
    }

    val images = listOf(
        R.drawable.splash,
        R.drawable.splash_1,
        R.drawable.splash_2
    )
    val totalPages = images.size
    var forward by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(initialPage = 0)

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = if (forward) {
                if (pagerState.currentPage < totalPages - 1) pagerState.currentPage + 1
                else {
                    forward = false
                    pagerState.currentPage - 1
                }
            } else {
                if (pagerState.currentPage > 0) pagerState.currentPage - 1
                else {
                    forward = true
                    pagerState.currentPage + 1
                }
            }
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = totalPages,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Onboarding Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .padding(80.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.title_name),
                contentDescription = "Title Name"
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 15.dp, vertical = 100.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pagerState.currentPage == 0){
                    Text(
                        text = stringResource(id = R.string.welcome_text),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = stringResource(id = R.string.welcome_text1),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = stringResource(id = R.string.discover_text),
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 10.dp,bottom = 5.dp)
                    )
                } else if(pagerState.currentPage == 1){
                    Text(
                        text = stringResource(id = R.string.welcome_car),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = stringResource(id = R.string.welcome_car1),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = stringResource(id = R.string.browser_text),
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 10.dp,bottom = 5.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.welcome_market),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = stringResource(id = R.string.welcome_market1),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 35.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = stringResource(id = R.string.buy_text),
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 10.dp,bottom = 5.dp)
                    )

                }


                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().align(Alignment.Start).padding(top = 10.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onContinueClicked()},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 30.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(
                text = stringResource(id = R.string.button_started),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}