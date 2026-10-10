package com.ngxyzaudax.radiacode.protocol

import java.nio.charset.Charset

fun decodeCp1251(bytes: ByteArray): String {
    val charset = runCatching { Charset.forName("windows-1251") }.getOrNull()
    return if (charset != null) String(bytes, charset) else String(bytes, Charsets.ISO_8859_1)
}
