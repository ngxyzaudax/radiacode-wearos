package com.ngxyzaudax.radiacode.protocol

fun sfrSupportsLedsOn(text: String): Boolean =
    text.lines().any { line ->
        line.contains("VSFR_LEDS_ON") || line.contains("Addr=0x00000545")
    }
