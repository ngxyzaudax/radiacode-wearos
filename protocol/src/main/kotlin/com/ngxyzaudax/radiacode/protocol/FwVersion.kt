package com.ngxyzaudax.radiacode.protocol

data class FirmwareVersion(
    val major: Int,
    val minor: Int,
    val date: String,
)

data class DeviceVersions(
    val boot: FirmwareVersion,
    val target: FirmwareVersion,
)

fun decodeFwVersion(response: ByteArray): DeviceVersions {
    val candidates = fwVersionCandidates(response)
    for (candidate in candidates) {
        runCatching { decodeFwVersionBody(candidate) }.getOrNull()?.let { return it }
    }
    return decodeFwVersionBody(response)
}

fun isCompatibleTargetFirmware(versions: DeviceVersions): Boolean {
    val major = versions.target.major
    val minor = versions.target.minor
    return major > 4 || (major == 4 && minor >= 8)
}

private fun fwVersionCandidates(response: ByteArray): List<ByteArray> {
    val candidates = ArrayList<ByteArray>()
    var current = response
    candidates.add(current)
    while (current.size > 51 && current.last() == 0.toByte()) {
        current = current.copyOfRange(0, current.size - 1)
        candidates.add(current)
    }
    if (current.size >= 4) {
        val reader = ByteBufferReader(current)
        if (reader.readU32Le() == 1L) {
            candidates.add(reader.data())
        }
    }
    return candidates
}

private fun decodeFwVersionBody(response: ByteArray): DeviceVersions {
    val reader = ByteBufferReader(response)
    val bootMinor = reader.readU16Le()
    val bootMajor = reader.readU16Le()
    val bootDate = reader.readLengthPrefixedAscii()
    val targetMinor = reader.readU16Le()
    val targetMajor = reader.readU16Le()
    val targetDate = reader.readLengthPrefixedAscii().trimEnd('\u0000')
    if (reader.remaining() != 0) {
        throw ProtocolException("fw_version has ${reader.remaining()} trailing bytes")
    }
    return DeviceVersions(
        boot = FirmwareVersion(bootMajor, bootMinor, bootDate),
        target = FirmwareVersion(targetMajor, targetMinor, targetDate),
    )
}
