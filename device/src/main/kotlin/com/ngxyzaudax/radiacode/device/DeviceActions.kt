package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.protocol.Command
import com.ngxyzaudax.radiacode.protocol.ProtocolException
import com.ngxyzaudax.radiacode.protocol.VirtSfr
import com.ngxyzaudax.radiacode.protocol.VirtString
import com.ngxyzaudax.radiacode.protocol.parseWriteVirtStringRetcode

internal class DeviceActions(
    private val vsfr: VsfrIo,
    private val executeRaw: suspend (Command, ByteArray) -> ByteArray,
) {
    suspend fun doseReset(): Unit {
        vsfr.writeVsfr(VirtSfr.DoseReset, 0)
    }

    suspend fun spectrumReset(): Unit {
        val args = ByteArray(8)
        val id = VirtString.Spectrum.id
        args[0] = (id and 0xFF).toByte()
        args[1] = ((id shr 8) and 0xFF).toByte()
        args[2] = ((id shr 16) and 0xFF).toByte()
        args[3] = ((id shr 24) and 0xFF).toByte()
        val body = executeRaw(Command.WrVirtString, args)
        if (parseWriteVirtStringRetcode(body) != 1L) {
            throw ProtocolException("Spectrum reset rejected")
        }
    }

    suspend fun syncClock(setTime: suspend () -> Unit): Unit {
        setTime()
        if (!applyDeviceMonotonicClock(vsfr)) {
            throw ProtocolException("Device clock commit rejected")
        }
    }
}
