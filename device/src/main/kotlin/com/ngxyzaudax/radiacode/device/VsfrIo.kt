package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.Command
import com.ngxyzaudax.radiacode.protocol.ProtocolException
import com.ngxyzaudax.radiacode.protocol.SequenceCounter
import com.ngxyzaudax.radiacode.protocol.VirtSfr
import com.ngxyzaudax.radiacode.protocol.parseVsfrBatch
import com.ngxyzaudax.radiacode.protocol.parseVsfrOptional
import com.ngxyzaudax.radiacode.protocol.parseVsfrRetcode
import com.ngxyzaudax.radiacode.protocol.parseVsfrU32
import com.ngxyzaudax.radiacode.protocol.virtSfrBatchReadArgs
import com.ngxyzaudax.radiacode.protocol.virtSfrBatchWriteArgs
import com.ngxyzaudax.radiacode.protocol.virtSfrReadArgs
import com.ngxyzaudax.radiacode.protocol.virtSfrWriteArgs

private const val component: String = "VsfrIo"

internal class VsfrIo(
    private val execute: suspend (Command, Int, ByteArray) -> ByteArray,
    private val sequence: SequenceCounter,
) {
    suspend fun readVsfrU32(sfr: VirtSfr): Int {
        val seq = sequence.advance()
        val args = readArgs(sfr)
        val body = execute(Command.RdVirtSfr, seq, args)
        return parseVsfrU32(body)
    }

    suspend fun readVsfrF32(sfr: VirtSfr): Float =
        Float.fromBits(readVsfrU32(sfr))

    suspend fun readVsfrOptional(sfr: VirtSfr): Int? {
        val seq = sequence.advance()
        val body = execute(Command.RdVirtSfr, seq, readArgs(sfr))
        return parseVsfrOptional(body)
    }

    suspend fun writeVsfr(sfr: VirtSfr, value: Int): Unit {
        if (!writeVsfrOptional(sfr, value)) {
            throw ProtocolException("VirtSfr write rejected id=${sfr.id}")
        }
    }

    suspend fun writeVsfrOptional(sfr: VirtSfr, value: Int): Boolean =
        writeVsfrRetcode(sfr, value) == 1L

    suspend fun writeVsfrRetcode(sfr: VirtSfr, value: Int): Long {
        val seq = sequence.advance()
        val args = writeArgs(sfr, value)
        val body = execute(Command.WrVirtSfr, seq, args)
        return parseVsfrRetcode(body)
    }

    suspend fun readVsfrBatch(sfrs: List<VirtSfr>): List<Int> =
        runCatching { readVsfrBatchDirect(sfrs) }
            .getOrElse { readVsfrBatchSerial(sfrs) }

    suspend fun writeVsfrBatch(pairs: List<Pair<VirtSfr, Int>>): Unit =
        runCatching { writeVsfrBatchDirect(pairs) }
            .getOrElse { writeVsfrBatchSerial(pairs) }

    private suspend fun readVsfrBatchDirect(sfrs: List<VirtSfr>): List<Int> {
        val seq = sequence.advance()
        val args = batchReadArgs(sfrs)
        val body = execute(Command.RdVirtSfrBatch, seq, args)
        return parseVsfrBatch(body)
    }

    /**
     * Firmware revisions differ in which registers they expose, so one unsupported register must
     * not discard the values that were read successfully. Unreadable registers report zero.
     */
    private suspend fun readVsfrBatchSerial(sfrs: List<VirtSfr>): List<Int> =
        sfrs.map { sfr ->
            runCatching { readVsfrU32(sfr) }.getOrElse { error ->
                RadiaLog.warn(component, "VirtSfr unreadable id=${sfr.name} ${error.message}")
                0
            }
        }

    private suspend fun writeVsfrBatchDirect(pairs: List<Pair<VirtSfr, Int>>): Unit {
        val seq = sequence.advance()
        val args = batchWriteArgs(pairs)
        val body = execute(Command.WrVirtSfrBatch, seq, args)
        if (com.ngxyzaudax.radiacode.protocol.parseVsfrRetcode(body) != 1L) {
            throw ProtocolException("VirtSfr batch write rejected")
        }
    }

    private suspend fun writeVsfrBatchSerial(pairs: List<Pair<VirtSfr, Int>>): Unit =
        pairs.forEach { (sfr, value) ->
            if (!writeVsfrOptionalQuietly(sfr, value)) {
                RadiaLog.warn(component, "VirtSfr write rejected id=${sfr.name}")
            }
        }

    private suspend fun writeVsfrOptionalQuietly(sfr: VirtSfr, value: Int): Boolean =
        runCatching { writeVsfrOptional(sfr, value) }.getOrDefault(false)

    private fun readArgs(sfr: VirtSfr): ByteArray = virtSfrReadArgs(sfr)

    private fun writeArgs(sfr: VirtSfr, value: Int): ByteArray = virtSfrWriteArgs(sfr, value)

    private fun batchReadArgs(sfrs: List<VirtSfr>): ByteArray = virtSfrBatchReadArgs(sfrs)

    private fun batchWriteArgs(pairs: List<Pair<VirtSfr, Int>>): ByteArray =
        virtSfrBatchWriteArgs(pairs)
}
