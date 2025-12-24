package com.agrid.flutter

import android.graphics.BitmapFactory

class SnapshotSender {
    fun sendFullSnapshot(
        imageBytes: ByteArray,
        id: Int,
        x: Int,
        y: Int,
    ) {
        // No-op on Android for this fork (internal replay API not used)
    }

    fun sendMetaEvent(
        width: Int,
        height: Int,
        screen: String,
    ) {
        // No-op on Android for this fork (internal replay API not used)
    }
}
