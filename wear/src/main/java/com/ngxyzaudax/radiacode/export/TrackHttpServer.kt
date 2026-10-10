package com.ngxyzaudax.radiacode.export

import android.net.Uri
import com.ngxyzaudax.radiacode.track.listTrackFiles
import fi.iki.elonen.NanoHTTPD
import java.io.File

private const val READ_TIMEOUT_MS: Int = 5_000
private const val PREFERRED_PORT: Int = 8080
private const val ARCHIVE_PATH: String = "/tracks.zip"
private const val ARCHIVE_NAME: String = "radiacode-tracks.zip"
private const val TRACK_PREFIX: String = "/tracks/"
private const val HTML_MIME: String = "text/html; charset=utf-8"
private const val OCTET_MIME: String = "application/octet-stream"
private const val ZIP_MIME: String = "application/zip"

class TrackHttpServer(
    port: Int,
    private val tracksDirectory: File,
    private val cacheDirectory: File,
) : NanoHTTPD(port) {
    private val archiveLock: Any = Any()

    override fun serve(session: IHTTPSession): Response = when {
        session.method != Method.GET -> notFound()
        session.uri == "/" -> pageResponse()
        session.uri == ARCHIVE_PATH -> archiveResponse()
        session.uri.startsWith(TRACK_PREFIX) -> fileResponse(session.uri)
        else -> notFound()
    }

    override fun stop(): Unit {
        super.stop()
        archiveFile().delete()
    }

    private fun pageResponse(): Response =
        newFixedLengthResponse(
            Response.Status.OK,
            HTML_MIME,
            renderTrackExportPage(listTrackFiles(tracksDirectory)),
        )

    private fun fileResponse(uri: String): Response {
        val file = resolveTrack(uri) ?: return notFound()
        val response = newFixedLengthResponse(Response.Status.OK, OCTET_MIME, file.inputStream(), file.length())
        response.addHeader("Content-Disposition", attachmentHeader(file.name))
        return response
    }

    private fun archiveResponse(): Response {
        val archive = synchronized(archiveLock) {
            buildTrackArchive(listTrackFiles(tracksDirectory), archiveFile())
        }
        val response = newFixedLengthResponse(Response.Status.OK, ZIP_MIME, archive.inputStream(), archive.length())
        response.addHeader("Content-Disposition", attachmentHeader(archive.name))
        return response
    }

    private fun resolveTrack(uri: String): File? {
        val name = Uri.decode(uri.removePrefix(TRACK_PREFIX))
        if (!isSafeTrackName(name)) return null
        return listTrackFiles(tracksDirectory).firstOrNull { file -> file.name == name }
    }

    private fun archiveFile(): File = File(cacheDirectory, ARCHIVE_NAME)

    companion object {
        fun start(tracksDirectory: File, cacheDirectory: File): TrackHttpServer =
            bind(PREFERRED_PORT, tracksDirectory, cacheDirectory)
                ?: bind(0, tracksDirectory, cacheDirectory)
                ?: error("Track export server could not bind a port")

        private fun bind(port: Int, tracksDirectory: File, cacheDirectory: File): TrackHttpServer? {
            val server = TrackHttpServer(port, tracksDirectory, cacheDirectory)
            return runCatching { server.start(READ_TIMEOUT_MS, false) }
                .onFailure { server.stop() }
                .map { server }
                .getOrNull()
        }
    }
}

private fun notFound(): NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, HTML_MIME, "Not found")

private fun isSafeTrackName(name: String): Boolean =
    name.isNotBlank() &&
        !name.contains('/') &&
        !name.contains('\\') &&
        name.endsWith(".rctrk", ignoreCase = true)

private fun attachmentHeader(fileName: String): String =
    "attachment; filename=\"${fileName.replace("\"", "")}\""
