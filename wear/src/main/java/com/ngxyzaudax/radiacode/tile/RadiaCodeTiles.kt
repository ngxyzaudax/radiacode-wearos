package com.ngxyzaudax.radiacode.tile

import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private typealias TileFuture = ListenableFuture<TileBuilders.Tile>
private typealias ResourcesFuture = ListenableFuture<Resources>

abstract class RadiaCodeTileService : TileService() {
    protected abstract fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile

    final override fun onTileRequest(requestParams: RequestBuilders.TileRequest): TileFuture =
        tileExecutor.submit<TileBuilders.Tile> { tile(requestParams) }

    final override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ResourcesFuture = Futures.immediateFuture(emptyTileResources())
}

class RatesTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        buildRatesTile(requestParams, this)
}

class DoseRateTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        buildDoseRateTile(requestParams, this)
}

class CountRateTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        buildCountRateTile(requestParams, this)
}

class StatusTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        buildStatusTile(requestParams, this)
}

class TrackTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        handleTrackTileAction(this, requestParams)
        return buildTrackTile(requestParams, this)
    }
}

class AccumDoseTileService : RadiaCodeTileService() {
    override fun tile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        buildAccumDoseTile(requestParams, this)
}
