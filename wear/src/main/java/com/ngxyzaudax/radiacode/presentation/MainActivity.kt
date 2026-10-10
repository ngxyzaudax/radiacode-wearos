package com.ngxyzaudax.radiacode.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.presentation.theme.RadiaCodeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application as RadiaCodeApp)
    }

    private var pendingAction: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.all { it }) {
            pendingAction?.invoke()
            pendingAction = null
            viewModel.autoConnect(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RadiaLog.info("MainActivity", "Created")
        setContent {
            RadiaCodeTheme {
                RadiaCodeNavGraph(
                    viewModel = viewModel,
                    onRequestPermissions = { action -> requestPermissionsIfNeeded(action) },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        RadiaLog.info("MainActivity", "Brought to front")
    }

    override fun onDestroy() {
        viewModel.unbind(this)
        super.onDestroy()
    }

    private fun requestPermissionsIfNeeded(onGranted: (() -> Unit)? = null): Unit {
        if (DevicePermissions.hasAll(this)) {
            onGranted?.invoke()
            return
        }
        pendingAction = onGranted
        permissionLauncher.launch(DevicePermissions.required)
    }
}
