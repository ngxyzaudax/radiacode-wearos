package com.ngxyzaudax.radiacode.tile

import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Executors

private const val TILE_THREAD_NAME: String = "radiacode-tiles"

/**
 * Tile layouts are built and serialised into protolayout protos, which is heavy enough to drop
 * frames when it runs on the main looper. A single background thread keeps the six tile services
 * off the UI thread while still serialising their builds against each other.
 */
internal val tileExecutor: ListeningExecutorService =
    MoreExecutors.listeningDecorator(
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, TILE_THREAD_NAME).apply { isDaemon = true }
        },
    )
