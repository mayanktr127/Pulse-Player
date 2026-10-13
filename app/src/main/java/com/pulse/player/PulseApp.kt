package com.pulse.player

import android.app.Application
import com.pulse.player.data.local.AppDatabase
import com.pulse.player.data.repository.DownloadRepository
import com.pulse.player.data.repository.LocalMediaRepository
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.data.repository.YouTubeRepository
import com.pulse.player.player.Media3PlayerManager

class PulseApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var youTubeRepository: YouTubeRepository
        private set

    lateinit var localMediaRepository: LocalMediaRepository
        private set

    lateinit var mediaRepository: MediaRepository
        private set

    lateinit var downloadRepository: DownloadRepository
        private set

    lateinit var playerManager: Media3PlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        youTubeRepository = YouTubeRepository(this)
        localMediaRepository = LocalMediaRepository(this)
        mediaRepository = MediaRepository(database, youTubeRepository, localMediaRepository)
        downloadRepository = DownloadRepository(this, database)
        playerManager = Media3PlayerManager(this)
    }

    override fun onTerminate() {
        playerManager.release()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: PulseApp
            private set
    }
}
