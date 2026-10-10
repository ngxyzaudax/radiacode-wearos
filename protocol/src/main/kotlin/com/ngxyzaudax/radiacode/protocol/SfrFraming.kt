package com.ngxyzaudax.radiacode.protocol

fun virtSfrReadArgs(sfr: VirtSfr): ByteArray = u32Le(sfr.id)

fun virtSfrWriteArgs(sfr: VirtSfr, value: Int): ByteArray {
    val args = ByteArray(8)
    u32Le(sfr.id).copyInto(args, 0)
    u32Le(value).copyInto(args, 4)
    return args
}

fun virtSfrBatchReadArgs(sfrs: List<VirtSfr>): ByteArray {
    val args = ByteArray(4 + sfrs.size * 4)
    u32Le(sfrs.size).copyInto(args, 0)
    sfrs.forEachIndexed { index, sfr -> u32Le(sfr.id).copyInto(args, 4 + index * 4) }
    return args
}

fun virtSfrBatchWriteArgs(pairs: List<Pair<VirtSfr, Int>>): ByteArray {
    val args = ByteArray(4 + pairs.size * 8)
    u32Le(pairs.size).copyInto(args, 0)
    pairs.forEachIndexed { index, (sfr, value) ->
        virtSfrWriteArgs(sfr, value).copyInto(args, 4 + index * 8)
    }
    return args
}

fun buildReadVirtSfr(sfr: VirtSfr, seq: Int): ByteArray =
    buildRequest(Command.RdVirtSfr, seq, virtSfrReadArgs(sfr))

fun buildWriteVirtSfr(sfr: VirtSfr, value: Int, seq: Int): ByteArray =
    buildRequest(Command.WrVirtSfr, seq, virtSfrWriteArgs(sfr, value))

fun buildWriteVirtString(virtString: VirtString, payload: ByteArray, seq: Int): ByteArray {
    val args = ByteArray(8 + payload.size)
    u32Le(virtString.id).copyInto(args, 0)
    u32Le(payload.size).copyInto(args, 4)
    payload.copyInto(args, 8)
    return buildRequest(Command.WrVirtString, seq, args)
}

fun buildReadVirtSfrBatch(sfrs: List<VirtSfr>, seq: Int): ByteArray =
    buildRequest(Command.RdVirtSfrBatch, seq, virtSfrBatchReadArgs(sfrs))

fun buildWriteVirtSfrBatch(pairs: List<Pair<VirtSfr, Int>>, seq: Int): ByteArray =
    buildRequest(Command.WrVirtSfrBatch, seq, virtSfrBatchWriteArgs(pairs))

fun parseVsfrRetcode(response: ByteArray): Long {
    val reader = ByteBufferReader(response)
    return reader.readU32Le()
}

fun parseVsfrU32(response: ByteArray): Int {
    val reader = ByteBufferReader(response)
    val retcode = reader.readU32Le()
    if (retcode != 1L) throw ProtocolException("VirtSfr retcode $retcode")
    return reader.readU32Le().toInt()
}

fun parseVsfrOptional(response: ByteArray): Int? {
    val reader = ByteBufferReader(response)
    return when (reader.readU32Le()) {
        1L -> reader.readU32Le().toInt()
        0L -> null
        else -> throw ProtocolException("VirtSfr optional retcode failed")
    }
}

fun parseVsfrBatch(response: ByteArray): List<Int> {
    val reader = ByteBufferReader(response)
    val retcode = reader.readU32Le()
    if (retcode != 1L) throw ProtocolException("VirtSfr batch retcode $retcode")
    val count = reader.readU32Le().toInt()
    return List(count) { reader.readU32Le().toInt() }
}

fun parseWriteVirtStringRetcode(response: ByteArray): Long {
    val reader = ByteBufferReader(response)
    return reader.readU32Le()
}

private fun u32Le(value: Int): ByteArray = byteArrayOf(
    (value and 0xFF).toByte(),
    ((value shr 8) and 0xFF).toByte(),
    ((value shr 16) and 0xFF).toByte(),
    ((value shr 24) and 0xFF).toByte(),
)
