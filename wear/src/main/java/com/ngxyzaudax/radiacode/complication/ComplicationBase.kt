package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.ngxyzaudax.radiacode.data.LiveReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRangedComplicationService : SuspendingComplicationDataSourceService() {
    protected fun reading(): LiveReading = ComplicationReading.load(this)

    final override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData =
        withContext(Dispatchers.IO) {
            runCatching { buildComplication(request) }
                .getOrElse { shortComplication("RadiaCode", "--") }
        }

    protected abstract suspend fun buildComplication(request: ComplicationRequest): ComplicationData

    override fun getPreviewData(type: ComplicationType): ComplicationData? = previewComplication(type)
}
