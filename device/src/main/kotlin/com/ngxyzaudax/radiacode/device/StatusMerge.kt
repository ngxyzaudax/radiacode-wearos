package com.ngxyzaudax.radiacode.device

fun validBattery(value: Float): Boolean = value in 0f..150f

fun validTemperature(value: Float): Boolean = value in -40f..85f

fun mergeDeviceStatus(carry: DeviceStatus, fresh: DeviceStatus): DeviceStatus =
    DeviceStatus(
        batteryPercent = fresh.batteryPercent?.takeIf(::validBattery) ?: carry.batteryPercent,
        temperatureC = fresh.temperatureC?.takeIf(::validTemperature) ?: carry.temperatureC,
        rssiDbm = fresh.rssiDbm ?: carry.rssiDbm,
        connected = fresh.connected,
        label = fresh.label ?: carry.label,
    )

fun mergeAccumulatedDose(carry: AccumulatedDose?, fresh: AccumulatedDose?): AccumulatedDose? =
    fresh ?: carry
