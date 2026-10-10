package com.ngxyzaudax.radiacode.protocol

private const val USER_DATA_BYTES: Int = 16
private const val RAW_DATA_BYTES: Int = 8
private const val DOSE_RATE_DB_BYTES: Int = 16
private const val ACCEL_DATA_BYTES: Int = 6
private const val EVENT_BYTES: Int = 4
private const val RAW_RATE_BYTES: Int = 6
private const val WAVEFORM8_SAMPLE_BYTES: Int = 8
private const val WAVEFORM16_SAMPLE_BYTES: Int = 16
private const val WAVEFORM14_SAMPLE_BYTES: Int = 14
private const val TEMPERATURE_OFFSET_RAW: Int = 2000
private const val TEMPERATURE_SCALE: Float = 100f
private const val BATTERY_SCALE: Float = 100f

internal fun parseRecordPayload(reader: ByteBufferReader, header: RecordHeader): DataBufRecord? =
    when (header.kind) {
        RecordKind.RealTimeData -> parseRealTime(reader, header)?.let(DataBufRecord::RealTime)
        RecordKind.RareData -> parseRare(reader, header)?.let(DataBufRecord::Rare)
        RecordKind.RawData -> skipped(reader, header, RAW_DATA_BYTES)
        RecordKind.DoseRateDb -> skipped(reader, header, DOSE_RATE_DB_BYTES)
        RecordKind.UserData, RecordKind.ScheduleData -> skipped(reader, header, USER_DATA_BYTES)
        RecordKind.AccelData -> skipped(reader, header, ACCEL_DATA_BYTES)
        RecordKind.Event -> skipped(reader, header, EVENT_BYTES)
        RecordKind.RawCountRate, RecordKind.RawDoseRate -> skipped(reader, header, RAW_RATE_BYTES)
        RecordKind.Waveform8,
        RecordKind.Waveform16,
        RecordKind.Waveform14,
        -> skipWaveform(reader, header)
        RecordKind.Unknown -> null
    }

private fun parseRealTime(reader: ByteBufferReader, header: RecordHeader): RealTimeData? =
    runCatching {
        RealTimeData(
            header = header,
            countRateCps = reader.readF32Le(),
            doseRateRh = reader.readF32Le(),
            countRateErrPct = reader.readU16Le() / 10f,
            doseRateErrPct = reader.readU16Le() / 10f,
            flags = reader.readU16Le(),
            realTimeFlags = reader.readU8(),
        )
    }.getOrNull()

private fun parseRare(reader: ByteBufferReader, header: RecordHeader): RareData? =
    runCatching {
        val duration = reader.readU32Le()
        val dose = reader.readF32Le()
        val temperatureRaw = reader.readU16Le()
        val chargeRaw = reader.readU16Le()
        val flags = reader.readU16Le()
        RareData(
            header = header,
            durationSecs = duration,
            doseR = dose,
            temperatureC = (temperatureRaw - TEMPERATURE_OFFSET_RAW) / TEMPERATURE_SCALE,
            batteryPercent = chargeRaw / BATTERY_SCALE,
            flags = flags,
        )
    }.getOrNull()

/**
 * Waveform records carry a variable payload, so their length must be read from the record itself.
 * Skipping them correctly is what keeps later records in the same frame reachable.
 */
private fun skipWaveform(reader: ByteBufferReader, header: RecordHeader): DataBufRecord? =
    runCatching {
        val sampleCount = reader.readU16Le()
        reader.readU32Le()
        reader.skip(sampleCount * waveformSampleBytes(header.kind))
        DataBufRecord.Skipped(header)
    }.getOrNull()

private fun waveformSampleBytes(kind: RecordKind): Int = when (kind) {
    RecordKind.Waveform8 -> WAVEFORM8_SAMPLE_BYTES
    RecordKind.Waveform16 -> WAVEFORM16_SAMPLE_BYTES
    RecordKind.Waveform14 -> WAVEFORM14_SAMPLE_BYTES
    else -> 0
}

private fun skipped(reader: ByteBufferReader, header: RecordHeader, count: Int): DataBufRecord? =
    runCatching {
        reader.skip(count)
        DataBufRecord.Skipped(header)
    }.getOrNull()
