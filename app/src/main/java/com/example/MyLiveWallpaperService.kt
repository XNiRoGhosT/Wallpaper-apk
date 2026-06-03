package com.example

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.util.Log

class MyLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var isVisible = false

        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            if (visible) {
                 mediaPlayer?.start()
            } else {
                 mediaPlayer?.pause()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            playVideo(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releasePlayer()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        private fun playVideo(holder: SurfaceHolder) {
            try {
                releasePlayer()
                
                val sharedPrefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
                val videoUrl = sharedPrefs.getString("wallpaper_video_url", "https://assets.mixkit.co/videos/preview/mixkit-anime-girl-with-glowing-neon-umbrella-43951-large.mp4") 
                    ?: "https://assets.mixkit.co/videos/preview/mixkit-anime-girl-with-glowing-neon-umbrella-43951-large.mp4"

                mediaPlayer = MediaPlayer().apply {
                    setSurface(holder.surface)
                    setDataSource(applicationContext, Uri.parse(videoUrl))
                    isLooping = true
                    // Silent is preferred for wallpapers so it doesn't disturb user
                    setVolume(0f, 0f)
                    
                    setOnPreparedListener { mp ->
                        mp.start()
                        if (!isVisible) {
                            mp.pause()
                        }
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("MyLiveWallpaperService", "Error playing live wallpaper video direct or stream url", e)
            }
        }

        private fun releasePlayer() {
            mediaPlayer?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
            mediaPlayer = null
        }
    }
}
