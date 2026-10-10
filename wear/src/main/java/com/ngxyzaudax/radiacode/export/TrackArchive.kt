package com.ngxyzaudax.radiacode.export

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun buildTrackArchive(tracks: List<File>, destination: File): File {
    destination.parentFile?.mkdirs()
    ZipOutputStream(destination.outputStream().buffered()).use { zip ->
        tracks.forEach { file -> writeTrackEntry(zip, file) }
    }
    return destination
}

private fun writeTrackEntry(zip: ZipOutputStream, file: File): Unit {
    zip.putNextEntry(ZipEntry(file.name))
    file.inputStream().use { input -> input.copyTo(zip) }
    zip.closeEntry()
}
