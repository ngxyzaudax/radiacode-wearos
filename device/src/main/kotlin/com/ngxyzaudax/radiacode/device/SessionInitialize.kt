package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.Command
import com.ngxyzaudax.radiacode.protocol.DeviceVersions
import com.ngxyzaudax.radiacode.protocol.ProtocolException
import com.ngxyzaudax.radiacode.protocol.VirtString
import com.ngxyzaudax.radiacode.protocol.buildSetTimePayload
import com.ngxyzaudax.radiacode.protocol.decodeCp1251
import com.ngxyzaudax.radiacode.protocol.decodeFwVersion
import com.ngxyzaudax.radiacode.protocol.isCompatibleTargetFirmware
import com.ngxyzaudax.radiacode.protocol.parseSpecFormatVersion
import kotlinx.coroutines.delay

internal data class SessionInitResult(
    val serial: String,
    val versions: DeviceVersions,
    val specFormatVersion: Int,
    val baseTimeEpochMs: Long,
)

internal suspend fun initializeRadiaCodeSession(
    executeRaw: suspend (Command, ByteArray) -> ByteArray,
    readVirtString: suspend (VirtString) -> ByteArray,
    vsfr: VsfrIo,
    drain: suspend () -> Unit,
    deviceLabel: String,
    ignoreFirmwareCheck: Boolean = false,
): SessionInitResult {
    RadiaLog.debug("SessionInit", "Handshake SetExchange")
    executeRaw(Command.SetExchange, byteArrayOf(0x01, 0xFF.toByte(), 0x12, 0xFF.toByte()))
    drain()
    delay(250)
    drain()
    executeRaw(Command.SetTime, buildSetTimePayload())
    if (!applyDeviceMonotonicClock(vsfr)) {
        RadiaLog.warn("SessionInit", "DeviceTime rejected; continuing with SetTime only")
    }
    val baseTimeEpochMs = System.currentTimeMillis() + 128_000L
    runCatching { readVirtString(VirtString.SfrFile) }
        .onFailure { error -> RadiaLog.warn("SessionInit", "SfrFile load failed ${error.message}") }
    drain()
    delay(250)
    drain()
    val versions = readFirmwareVersions(executeRaw, drain)
    if (!ignoreFirmwareCheck && !isCompatibleTargetFirmware(versions)) {
        throw ProtocolException(
            "Incompatible firmware ${versions.target.major}.${versions.target.minor}",
        )
    }
    val configuration = decodeCp1251(readVirtString(VirtString.Configuration))
    var specFormatVersion = parseSpecFormatVersion(configuration)
    if (specFormatVersion == 0) {
        specFormatVersion = parseSpecFormatVersionLine(configuration)
    }
    val serial = runCatching {
        String(readVirtString(VirtString.SerialNumber), Charsets.UTF_8).trim()
    }.getOrDefault(deviceLabel)
    RadiaLog.info(
        "SessionInit",
        "Initialized serial=$serial target=${versions.target.major}.${versions.target.minor} " +
            "specFormat=$specFormatVersion",
    )
    return SessionInitResult(serial, versions, specFormatVersion, baseTimeEpochMs)
}

private suspend fun readFirmwareVersions(
    executeRaw: suspend (Command, ByteArray) -> ByteArray,
    drain: suspend () -> Unit,
): DeviceVersions {
    var lastError: Exception? = null
    repeat(3) { attempt ->
        if (attempt > 0) delay(250)
        drain()
        val body = executeRaw(Command.GetVersion, byteArrayOf())
        val decoded = runCatching { decodeFwVersion(body) }
        if (decoded.isSuccess) return decoded.getOrThrow()
        lastError = decoded.exceptionOrNull() as? Exception
        RadiaLog.warn("SessionInit", "GetVersion parse failed attempt=${attempt + 1}")
    }
    throw lastError ?: ProtocolException("GetVersion failed")
}

private fun parseSpecFormatVersionLine(configuration: String): Int =
    configuration.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith("SpecFormatVersion=") }
        ?.substringAfter('=')
        ?.toIntOrNull()
        ?: 0
