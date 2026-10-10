package com.ngxyzaudax.radiacode.presentation.screens.settings

import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.SignalFlags

enum class SignalEventKind {
    Sound,
    Vibration,
}

data class SignalEventSpec(
    val label: String,
    val getter: (SignalFlags) -> Boolean,
    val setter: (SignalFlags, Boolean) -> SignalFlags,
)

fun soundEventSpecs(): List<SignalEventSpec> = listOf(
    eventSpec("Dose rate OOS", { it.doseRateOutOfScale }, { f, v -> f.copy(doseRateOutOfScale = v) }),
    eventSpec("Count rate OOS", { it.countRateOutOfScale }, { f, v -> f.copy(countRateOutOfScale = v) }),
    eventSpec("Accum dose OOS", { it.doseOutOfScale }, { f, v -> f.copy(doseOutOfScale = v) }),
    eventSpec("Clicks", { it.clicks }, { f, v -> f.copy(clicks = v) }),
    eventSpec("Button feedback", { it.buttons }, { f, v -> f.copy(buttons = v) }),
    eventSpec("Connection", { it.connection }, { f, v -> f.copy(connection = v) }),
    eventSpec("Power", { it.power }, { f, v -> f.copy(power = v) }),
)

fun vibroEventSpecs(): List<SignalEventSpec> = listOf(
    eventSpec("Dose rate OOS", { it.doseRateOutOfScale }, { f, v -> f.copy(doseRateOutOfScale = v) }),
    eventSpec("Count rate OOS", { it.countRateOutOfScale }, { f, v -> f.copy(countRateOutOfScale = v) }),
    eventSpec("Accum dose OOS", { it.doseOutOfScale }, { f, v -> f.copy(doseOutOfScale = v) }),
    eventSpec("Button feedback", { it.buttons }, { f, v -> f.copy(buttons = v) }),
)

private fun eventSpec(
    label: String,
    getter: (SignalFlags) -> Boolean,
    setter: (SignalFlags, Boolean) -> SignalFlags,
): SignalEventSpec = SignalEventSpec(label, getter, setter)

fun updateSignalFlags(
    config: DeviceConfig,
    kind: SignalEventKind,
    spec: SignalEventSpec,
    enabled: Boolean,
): DeviceConfig =
    when (kind) {
        SignalEventKind.Sound -> config.copy(soundCtrl = spec.setter(config.soundCtrl, enabled))
        SignalEventKind.Vibration -> config.copy(vibroCtrl = spec.setter(config.vibroCtrl, enabled))
    }

fun signalFlags(config: DeviceConfig, kind: SignalEventKind): SignalFlags =
    when (kind) {
        SignalEventKind.Sound -> config.soundCtrl
        SignalEventKind.Vibration -> config.vibroCtrl
    }
