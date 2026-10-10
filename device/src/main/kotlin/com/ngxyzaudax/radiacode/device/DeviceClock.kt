package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.VirtSfr

internal suspend fun applyDeviceMonotonicClock(vsfr: VsfrIo): Boolean {
    val retcode = vsfr.writeVsfrRetcode(VirtSfr.DeviceTime, 0)
    if (retcode == 1L) return true
    RadiaLog.warn("DeviceClock", "DeviceTime commit rejected id=${VirtSfr.DeviceTime.id} retcode=$retcode")
    return false
}
