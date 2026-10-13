package com.pulse.player.data.model

data class StreamQuality(
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Long,
    val mimeType: String,
    val trackIndex: Int
) {
    companion object {
        val AUTO = StreamQuality(
            label = "Auto",
            width = 0,
            height = 0,
            bitrate = 0,
            mimeType = "",
            trackIndex = -1
        )
    }
}
