package com.ngxyzaudax.radiacode.protocol

data class Spectrum(
    val durationSec: Long,
    val a0: Float,
    val a1: Float,
    val a2: Float,
    val counts: IntArray,
) {
    fun channelEnergy(channel: Int): Float =
        a0 + a1 * channel + a2 * channel * channel

    override fun equals(other: Any?): Boolean =
        other is Spectrum &&
            durationSec == other.durationSec &&
            a0 == other.a0 &&
            a1 == other.a1 &&
            a2 == other.a2 &&
            counts.contentEquals(other.counts)

    override fun hashCode(): Int = counts.contentHashCode()
}

fun decodeSpectrum(data: ByteArray, formatVersion: Int): Spectrum {
    val reader = ByteBufferReader(data)
    val durationSec = reader.readU32Le()
    val a0 = reader.readF32Le()
    val a1 = reader.readF32Le()
    val a2 = reader.readF32Le()
    val counts = when (formatVersion) {
        0 -> decodeCountsV0(reader)
        1 -> decodeCountsV1(reader)
        else -> throw ProtocolException("Unknown spectrum format $formatVersion")
    }
    return Spectrum(durationSec, a0, a1, a2, counts)
}

private fun decodeCountsV0(reader: ByteBufferReader): IntArray {
    val counts = mutableListOf<Int>()
    while (reader.remaining() >= 4) counts.add(reader.readU32Le().toInt())
    return counts.toIntArray()
}

private fun decodeCountsV1(reader: ByteBufferReader): IntArray {
    val counts = mutableListOf<Int>()
    var last = 0
    while (reader.remaining() >= 2) {
        val packed = reader.readU16Le()
        val repeat = (packed shr 4) and 0x0FFF
        val vlen = packed and 0x0F
        repeat(repeat) {
            last = readCompressedCount(reader, vlen, last)
            counts.add(last)
        }
    }
    return counts.toIntArray()
}

private fun readI8(reader: ByteBufferReader): Int = reader.readBytes(1)[0].toInt()

private fun readI16Le(reader: ByteBufferReader): Int {
    val b = reader.readBytes(2)
    val value = (b[0].toInt() and 0xFF) or (b[1].toInt() shl 8)
    return if (value and 0x8000 != 0) value or -0x10000 else value
}

private fun readCompressedCount(reader: ByteBufferReader, vlen: Int, last: Int): Int =
    when (vlen) {
        0 -> 0
        1 -> reader.readU8()
        2 -> last + readI8(reader)
        3 -> last + readI16Le(reader)
        4 -> {
            val a = reader.readU8()
            val b = reader.readU8()
            val c = readI8(reader)
            last + ((c shl 16) or (b shl 8) or a)
        }
        5 -> last + reader.readI32Le()
        else -> throw ProtocolException("Unknown vlen $vlen")
    }

fun parseSpecFormatVersion(configIni: String): Int {
    val match = Regex("""SpecFormatVersion\s*=\s*(\d+)""", RegexOption.IGNORE_CASE).find(configIni)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
}
