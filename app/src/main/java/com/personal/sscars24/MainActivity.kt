package com.personal.sscars24

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.personal.sscars24.presentation.ui.login.LoginScreen
import com.personal.sscars24.presentation.ui.login.OtpScreen
import com.personal.sscars24.presentation.ui.login.RegistrationScreen
import com.personal.sscars24.presentation.ui.splash.OnboardingScreen
import com.personal.sscars24.presentation.ui.theme.SSCars24Theme
import com.personal.sscars24.presentation.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val loginViewmodel: LoginViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            SSCars24Theme {
                NavigationSetup(loginViewmodel)
                //CaptchaScreen(SITE_KEY)
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationSetup(loginViewmodel: LoginViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            OnboardingScreen(
                loginViewmodel = loginViewmodel,
                onContinueClicked = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                loginViewmodel = loginViewmodel,
                onLoginSuccess = { requestId, otp, remaining,phoneNumber ->
                    navController.navigate("otp") {
                        //popUpTo("login") { inclusive = true }
                    }
                    val entry = navController.getBackStackEntry("otp")
                    entry.savedStateHandle["request_id"] = requestId
                    entry.savedStateHandle["otp"] = otp
                    entry.savedStateHandle["remaining_otps"] = remaining
                    entry.savedStateHandle["phoneNumber"] = phoneNumber
                }
            )
        }
        composable("otp") { backStackEntry ->
            val saved = backStackEntry.savedStateHandle
            val requestId: String? = saved.get<String>("request_id")
            val otp: String? = saved.get<String>("otp")
            val remaining: Int? = saved.get<Int>("remaining_otps")
            val phoneNumber: String? = saved.get<String>("phoneNumber")

            OtpScreen(
                loginViewmodel = loginViewmodel,
                phoneNumber = phoneNumber,
                requestId = requestId,
                initialOtp = otp,
                remainingOtps = remaining ?: 0,
                onOtpSuccess = {
                    navController.navigate("registration") {
                    }
                    val entry = navController.getBackStackEntry("registration")
                    entry.savedStateHandle["phoneNumber"] = phoneNumber
                }
            )
        }

        composable("registration") { backStackEntry ->
            val saved = backStackEntry.savedStateHandle
            val phoneNumber: String? = saved.get<String>("phoneNumber")
            RegistrationScreen(loginViewmodel = loginViewmodel,phoneNumber,onRegisterSuccess = {
                navController.navigate("login") {
                }
            })
        }
    }
}
