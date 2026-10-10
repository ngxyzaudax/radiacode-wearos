package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import com.ngxyzaudax.radiacode.protocol.RealTimeData
import com.ngxyzaudax.radiacode.protocol.RareData
import com.ngxyzaudax.radiacode.protocol.Spectrum

data class DeviceInfo(
    val serial: String,
    val firmwareMajor: Int,
    val firmwareMinor: Int,
    val model: String,
)

data class MonitorSample(
    val timestampMs: Long,
    val doseRate: Float,
    val countRate: Float,
    val doseRateErrPct: Float,
    val countRateErrPct: Float,
    val doseUnit: DoseDisplayUnit,
    val countUnit: CountDisplayUnit,
)

data class AccumulatedDose(
    val dose: Float,
    val durationSecs: Long,
    val doseUnit: DoseDisplayUnit,
)

data class DeviceStatus(
    val batteryPercent: Float?,
    val temperatureC: Float?,
    val rssiDbm: Int?,
    val connected: Boolean,
    val label: String?,
)

data class MonitorState(
    val samples: List<MonitorSample>,
    val accumulated: AccumulatedDose?,
    val status: DeviceStatus,
)

data class SpectrumState(
    val live: Spectrum?,
    val accumulated: Spectrum?,
    val formatVersion: Int,
)

fun toMonitorSample(
    record: RealTimeData,
    doseUnit: DoseDisplayUnit,
    countUnit: CountDisplayUnit,
    timestampMs: Long,
): MonitorSample = MonitorSample(
    timestampMs = timestampMs,
    doseRate = com.ngxyzaudax.radiacode.protocol.doseDisplayFromRh(record.doseRateRh, doseUnit),
    countRate = com.ngxyzaudax.radiacode.protocol.countDisplayFromCps(record.countRateCps, countUnit),
    doseRateErrPct = record.doseRateErrPct,
    countRateErrPct = record.countRateErrPct,
    doseUnit = doseUnit,
    countUnit = countUnit,
)

fun toAccumulatedDose(rare: RareData, doseUnit: DoseDisplayUnit): AccumulatedDose =
    AccumulatedDose(
        dose = com.ngxyzaudax.radiacode.protocol.doseDisplayFromRh(rare.doseR, doseUnit),
        durationSecs = rare.durationSecs,
        doseUnit = doseUnit,
    )
