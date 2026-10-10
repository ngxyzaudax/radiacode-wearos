package com.ngxyzaudax.radiacode.ble

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentLinkedQueue

internal suspend fun drainNotificationsQuiet(
    queue: ConcurrentLinkedQueue<ByteArray>,
    maxMs: Long,
    peekThenQuiet: Boolean,
    linkAlive: () -> Boolean,
): Int {
    var drained = 0
    var lastReceivedAt = System.currentTimeMillis()
    if (peekThenQuiet) {
        val first = queue.poll()
        if (first == null) return 0
        drained = 1
        lastReceivedAt = System.currentTimeMillis()
    }
    val deadline = System.currentTimeMillis() + maxMs
    while (System.currentTimeMillis() < deadline) {
        if (!linkAlive()) break
        val chunk = queue.poll()
        if (chunk != null) {
            drained += 1
            lastReceivedAt = System.currentTimeMillis()
            continue
        }
        if (System.currentTimeMillis() - lastReceivedAt >= BleUuids.quietGapMs) break
        delay(20)
    }
    return drained
}
