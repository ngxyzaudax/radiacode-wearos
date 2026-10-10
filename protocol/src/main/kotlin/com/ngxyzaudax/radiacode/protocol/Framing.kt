package com.ngxyzaudax.radiacode.protocol

class SequenceCounter(private var value: Int = (System.nanoTime() and 0xFF).toInt() % 32) {
    fun advance(): Int {
        val seq = 0x80 + value
        value = (value + 1) % 32
        return seq
    }
}

fun buildRequest(command: Command, seq: Int, args: ByteArray): ByteArray {
    val body = ByteArray(4 + args.size)
    val cmd = command.code
    body[0] = (cmd and 0xFF).toByte()
    body[1] = ((cmd shr 8) and 0xFF).toByte()
    body[2] = 0
    body[3] = seq.toByte()
    args.copyInto(body, 4)
    val framed = ByteArray(4 + body.size)
    val len = body.size
    framed[0] = (len and 0xFF).toByte()
    framed[1] = ((len shr 8) and 0xFF).toByte()
    framed[2] = ((len shr 16) and 0xFF).toByte()
    framed[3] = ((len shr 24) and 0xFF).toByte()
    body.copyInto(framed, 4)
    return framed
}

fun requestHeader(command: Command, seq: Int): ByteArray =
    byteArrayOf(
        (command.code and 0xFF).toByte(),
        ((command.code shr 8) and 0xFF).toByte(),
        0,
        seq.toByte(),
    )

fun framedRequestHeader(request: ByteArray): ByteArray {
    if (request.size < 8) throw ProtocolException("Framed request too short")
    return request.copyOfRange(4, 8)
}

fun responseMatchesRequest(payload: ByteArray, expected: ByteArray): Boolean =
    payload.size >= 4 && payload.copyOfRange(0, 4).contentEquals(expected)

fun stripEchoedHeader(response: ByteArray, expected: ByteArray): ByteArray {
    if (response.size < 4) throw ProtocolException("Response too short")
    val header = response.copyOfRange(0, 4)
    if (!header.contentEquals(expected)) throw ProtocolException("Header mismatch")
    return response.copyOfRange(4, response.size)
}

fun buildReadVirtString(virtString: VirtString, seq: Int): ByteArray {
    val args = ByteArray(4)
    val id = virtString.id
    args[0] = (id and 0xFF).toByte()
    args[1] = ((id shr 8) and 0xFF).toByte()
    args[2] = ((id shr 16) and 0xFF).toByte()
    args[3] = ((id shr 24) and 0xFF).toByte()
    return buildRequest(Command.RdVirtString, seq, args)
}

fun parseVirtStringPayload(response: ByteArray): ByteArray {
    val reader = ByteBufferReader(response)
    val retcode = reader.readU32Le()
    if (retcode != 1L) throw ProtocolException("VirtString retcode $retcode")
    val flen = reader.readU32Le().toInt()
    var payload = reader.data()
    if (payload.size == flen + 1 && payload.last() == 0.toByte()) {
        payload = payload.copyOfRange(0, flen)
    }
    if (payload.size < flen) {
        throw ProtocolException("VirtString underrun need $flen have ${payload.size}")
    }
    if (payload.size > flen) {
        payload = payload.copyOfRange(0, flen)
    }
    return payload
}

class ResponseAssembler {
    private var remaining: Int = 0
    private val payload: ArrayList<Byte> = ArrayList()

    fun push(chunk: ByteArray): ByteArray? {
        if (remaining == 0) {
            if (chunk.size < 4) throw ProtocolException("Response chunk too short")
            val payloadLen = (chunk[0].toInt() and 0xFF) or
                ((chunk[1].toInt() and 0xFF) shl 8) or
                ((chunk[2].toInt() and 0xFF) shl 16) or
                (chunk[3].toInt() shl 24)
            if (payloadLen < 0) throw ProtocolException("Negative response length")
            remaining = 4 + payloadLen
            payload.clear()
            payload.addAll(chunk.drop(4))
        } else {
            payload.addAll(chunk.toList())
        }
        remaining -= chunk.size
        if (remaining == 0) return payload.toByteArray()
        return null
    }
}
