package com.example.realmatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.realmatch.ui.navigation.AppNavigation
import com.example.realmatch.ui.theme.RealmatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealmatchTheme {
                RequestLocalNetworkAccess()
                AppNavigation()
            }
        }
    }
}

/**
 * Android 17 (targetSdk 37) блокирует TCP к локальной сети без этого разрешения,
 * а dev-бэкенд на эмуляторе доступен по 10.0.2.2. До Android 17 разрешение не нужно.
 */
@Composable
private fun RequestLocalNetworkAccess() {
    if (Build.VERSION.SDK_INT < 37) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_LOCAL_NETWORK,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }
}
