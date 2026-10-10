package com.ngxyzaudax.radiacode

import android.app.Application
import com.ngxyzaudax.radiacode.data.AppSettingsStore
import com.ngxyzaudax.radiacode.data.DeviceConfigStore
import com.ngxyzaudax.radiacode.data.KnownDeviceStore
import com.ngxyzaudax.radiacode.data.LiveReadingStore
import com.ngxyzaudax.radiacode.data.ReadingSnapshotStore
import com.ngxyzaudax.radiacode.data.ScanUiStore

class RadiaCodeApp : Application() {
    lateinit var settingsStore: AppSettingsStore
        private set
    lateinit var liveReadingStore: LiveReadingStore
        private set
    lateinit var deviceConfigStore: DeviceConfigStore
        private set
    lateinit var readingSnapshotStore: ReadingSnapshotStore
        private set
    lateinit var knownDeviceStore: KnownDeviceStore
        private set
    lateinit var scanUiStore: ScanUiStore
        private set

    override fun onCreate() {
        super.onCreate()
        settingsStore = AppSettingsStore(this)
        liveReadingStore = LiveReadingStore()
        deviceConfigStore = DeviceConfigStore()
        readingSnapshotStore = ReadingSnapshotStore(this)
        knownDeviceStore = KnownDeviceStore(this)
        scanUiStore = ScanUiStore()
        AppForeground.track(this)
    }
}
