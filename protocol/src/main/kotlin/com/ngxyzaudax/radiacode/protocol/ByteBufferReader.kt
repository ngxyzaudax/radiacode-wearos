package com.ngxyzaudax.radiacode.protocol

class ByteBufferReader(private val data: ByteArray) {
    private var position: Int = 0

    fun remaining(): Int = data.size - position

    fun data(): ByteArray = data.copyOfRange(position, data.size)

    fun readU8(): Int {
        if (remaining() < 1) throw ProtocolException("Buffer underrun need 1 have ${remaining()}")
        return data[position++].toInt() and 0xFF
    }

    fun readU16Le(): Int {
        val bytes = readBytes(2)
        return (bytes[0].toInt() and 0xFF) or ((bytes[1].toInt() and 0xFF) shl 8)
    }

    fun readU32Le(): Long {
        val bytes = readBytes(4)
        return (bytes[0].toLong() and 0xFF) or
            ((bytes[1].toLong() and 0xFF) shl 8) or
            ((bytes[2].toLong() and 0xFF) shl 16) or
            ((bytes[3].toLong() and 0xFF) shl 24)
    }

    fun readI32Le(): Int {
        val bytes = readBytes(4)
        return (bytes[0].toInt() and 0xFF) or
            ((bytes[1].toInt() and 0xFF) shl 8) or
            ((bytes[2].toInt() and 0xFF) shl 16) or
            (bytes[3].toInt() shl 24)
    }

    fun readF32Le(): Float {
        val bits = readI32Le()
        return Float.fromBits(bits)
    }

    fun readBytes(count: Int): ByteArray {
        if (remaining() < count) throw ProtocolException("Buffer underrun need $count have ${remaining()}")
        val slice = data.copyOfRange(position, position + count)
        position += count
        return slice
    }

    fun skip(count: Int): Unit {
        readBytes(count)
    }

    fun readLengthPrefixedAscii(): String {
        val length = readU8()
        return String(readBytes(length), Charsets.US_ASCII)
    }
}
