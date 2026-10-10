package com.ngxyzaudax.radiacode.protocol

enum class RecordKind {
    RealTimeData,
    RawData,
    DoseRateDb,
    RareData,
    UserData,
    ScheduleData,
    AccelData,
    Event,
    RawCountRate,
    RawDoseRate,
    Waveform8,
    Waveform16,
    Waveform14,
    Unknown,
}

fun recordKindFromEntityGroup(entity: Int, group: Int): RecordKind = when (entity to group) {
    0 to 0 -> RecordKind.RealTimeData
    0 to 1 -> RecordKind.RawData
    0 to 2 -> RecordKind.DoseRateDb
    0 to 3 -> RecordKind.RareData
    0 to 4 -> RecordKind.UserData
    0 to 5 -> RecordKind.ScheduleData
    0 to 6 -> RecordKind.AccelData
    0 to 7 -> RecordKind.Event
    0 to 8 -> RecordKind.RawCountRate
    0 to 9 -> RecordKind.RawDoseRate
    1 to 1 -> RecordKind.Waveform8
    1 to 2 -> RecordKind.Waveform16
    1 to 3 -> RecordKind.Waveform14
    else -> RecordKind.Unknown
}

data class RecordHeader(
    val seq: Int,
    val kind: RecordKind,
    val entity: Int,
    val group: Int,
    val tsOffset: Int,
)

data class RealTimeData(
    val header: RecordHeader,
    val countRateCps: Float,
    val doseRateRh: Float,
    val countRateErrPct: Float,
    val doseRateErrPct: Float,
    val flags: Int,
    val realTimeFlags: Int,
)

data class RareData(
    val header: RecordHeader,
    val durationSecs: Long,
    val doseR: Float,
    val temperatureC: Float,
    val batteryPercent: Float,
    val flags: Int,
)

sealed class DataBufRecord {
    data class RealTime(val data: RealTimeData) : DataBufRecord()
    data class Rare(val data: RareData) : DataBufRecord()
    data class Skipped(val header: RecordHeader) : DataBufRecord()
}

data class DataBufFrame(
    val records: List<DataBufRecord>,
    val realTimeRecords: List<RealTimeData>,
    val latestRare: RareData?,
)

fun decodeDataBuf(data: ByteArray): DataBufFrame {
    val reader = ByteBufferReader(data)
    val records = mutableListOf<DataBufRecord>()
    while (reader.remaining() >= 7) {
        val seq = reader.readU8()
        val entity = reader.readU8()
        val group = reader.readU8()
        val tsOffset = reader.readI32Le()
        val kind = recordKindFromEntityGroup(entity, group)
        val header = RecordHeader(seq, kind, entity, group, tsOffset)
        val record = parseRecordPayload(reader, header) ?: break
        records.add(record)
    }
    val realTime = records.mapNotNull { (it as? DataBufRecord.RealTime)?.data }
    val latestRare = records.mapNotNull { (it as? DataBufRecord.Rare)?.data }.lastOrNull()
    return DataBufFrame(records, realTime, latestRare)
}

fun realTimeRecords(frame: DataBufFrame): List<RealTimeData> = frame.realTimeRecords

fun latestRareRecord(frame: DataBufFrame): RareData? = frame.latestRare
