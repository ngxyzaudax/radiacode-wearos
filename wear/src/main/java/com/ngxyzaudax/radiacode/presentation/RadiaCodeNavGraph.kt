package com.ngxyzaudax.radiacode.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.ngxyzaudax.radiacode.presentation.screens.DashboardScreen
import com.ngxyzaudax.radiacode.presentation.screens.TrackDetailScreen
import com.ngxyzaudax.radiacode.presentation.screens.TrackExportScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.AlarmSettingsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.AlarmEditorScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.AlarmKey
import com.ngxyzaudax.radiacode.presentation.screens.settings.AppSettingsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.DeviceActionsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.DisplaySettingsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.SettingsMenuScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.SignalEventsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.SignalEventKind
import com.ngxyzaudax.radiacode.presentation.screens.settings.SignalSettingsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.UnitsSettingsScreen
import com.ngxyzaudax.radiacode.presentation.screens.settings.soundEventSpecs
import com.ngxyzaudax.radiacode.presentation.screens.settings.vibroEventSpecs

@Composable
fun RadiaCodeNavGraph(
    viewModel: MainViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
): Unit {
    val context = LocalContext.current
    val navController = rememberSwipeDismissableNavController()
    val reading by viewModel.liveReading.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val deviceConfig by viewModel.pendingDeviceConfig.collectAsState()
    val discovered by viewModel.discovered.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val monitorState by viewModel.monitorState.collectAsState()
    val spectrumState by viewModel.spectrumState.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    LaunchedEffect(reading.connected) {
        if (!reading.connected) {
            val route = navController.currentBackStackEntry?.destination?.route
            if (route != null && route != AppRoute.Dashboard.route && route != AppRoute.TrackExport.route) {
                navController.popBackStack(AppRoute.Dashboard.route, inclusive = false)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (settings.autoReconnect && DevicePermissions.hasAll(context) && !reading.connected) {
            viewModel.autoConnect(context)
        }
    }

    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = AppRoute.Dashboard.route,
        ) {
            composable(AppRoute.Dashboard.route) {
                DashboardScreen(
                    reading = reading,
                    monitorState = monitorState,
                    spectrumState = spectrumState,
                    deviceInfo = deviceInfo,
                    discovered = discovered,
                    scanState = scanState,
                    tracks = tracks,
                    onOpenSettings = {
                        if (reading.connected) navController.navigate(AppRoute.Settings.route)
                    },
                    onScan = permissionAction(context, onRequestPermissions) { viewModel.scan(context) },
                    onConnect = { device ->
                        permissionAction(context, onRequestPermissions) {
                            viewModel.connect(device, context)
                        }.invoke()
                    },
                    onDisconnect = { viewModel.disconnect(context) },
                    onStartTrack = permissionAction(context, onRequestPermissions) {
                        viewModel.startTrack(context)
                    },
                    onStopTrack = { viewModel.stopTrack(context) },
                    onOpenTrack = { file ->
                        navController.navigate(AppRoute.trackDetailRoute(file.name))
                    },
                    onExportTracks = exportTracksAction(tracks) {
                        navController.navigate(AppRoute.TrackExport.route)
                    },
                    onResetDose = { viewModel.doseReset(context) },
                    onResetSpectrum = { viewModel.spectrumReset(context) },
                )
            }
            composable(AppRoute.TrackExport.route) {
                TrackExportScreen()
            }
            composable(AppRoute.TrackDetail.route) { entry ->
                val fileName = entry.arguments?.getString("fileName") ?: return@composable
                val file = viewModel.trackFile(fileName) ?: return@composable
                TrackDetailScreen(
                    file = file,
                    onDelete = {
                        viewModel.deleteTrack(file)
                        navController.popBackStack()
                    },
                )
            }
            composable(AppRoute.Settings.route) {
                SettingsMenuScreen(onNavigate = { navController.navigate(it.route) })
            }
            composable(AppRoute.SettingsUnits.route) {
                UnitsSettingsScreen(
                    config = deviceConfig,
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                )
            }
            composable(AppRoute.SettingsAlarms.route) {
                AlarmSettingsScreen(
                    config = deviceConfig,
                    onNavigateEditor = { key ->
                        navController.navigate(AppRoute.alarmValueRoute(key.routeKey))
                    },
                )
            }
            composable(AppRoute.SettingsAlarmValue.route) { entry ->
                val key = entry.arguments?.getString("key") ?: return@composable
                val alarmKey = AlarmKey.fromRouteKey(key) ?: return@composable
                AlarmEditorScreen(
                    config = deviceConfig,
                    key = alarmKey,
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppRoute.SettingsSignals.route) {
                SignalSettingsScreen(
                    config = deviceConfig,
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                    onNavigate = { navController.navigate(it.route) },
                )
            }
            composable(AppRoute.SettingsSoundEvents.route) {
                SignalEventsScreen(
                    title = "Other sound events",
                    config = deviceConfig,
                    kind = SignalEventKind.Sound,
                    specs = soundEventSpecs(),
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                )
            }
            composable(AppRoute.SettingsVibroEvents.route) {
                SignalEventsScreen(
                    title = "Other vibration events",
                    config = deviceConfig,
                    kind = SignalEventKind.Vibration,
                    specs = vibroEventSpecs(),
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                )
            }
            composable(AppRoute.SettingsDisplay.route) {
                DisplaySettingsScreen(
                    config = deviceConfig,
                    onUpdate = viewModel::setPendingDeviceConfig,
                    onApply = { viewModel.applyDeviceConfig(context) },
                )
            }
            composable(AppRoute.SettingsApp.route) {
                AppSettingsScreen(settings = settings, onUpdate = viewModel::updateSettings)
            }
            composable(AppRoute.SettingsDeviceActions.route) {
                DeviceActionsScreen(
                    onSyncClock = { viewModel.syncClock(context) },
                    onLoadConfig = { viewModel.loadDeviceConfig(context) },
                )
            }
        }
    }
}

private fun exportTracksAction(tracks: List<java.io.File>, navigate: () -> Unit): (() -> Unit)? =
    if (tracks.isEmpty()) null else navigate

private fun permissionAction(
    context: android.content.Context,
    onRequestPermissions: (() -> Unit) -> Unit,
    action: () -> Unit,
): () -> Unit = {
    if (DevicePermissions.hasAll(context)) action() else onRequestPermissions(action)
}
