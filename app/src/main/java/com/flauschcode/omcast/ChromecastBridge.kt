package com.flauschcode.omcast

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class ChromecastBridge(context: Context) {

    private var myYouTubePlayer: YouTubePlayer? = null

    init {
        ChromecastYouTubePlayerContext(CastContext.getSharedInstance(context).sessionManager,
            object: ChromecastConnectionListener {
                override fun onChromecastConnecting() {
                    Log.d(javaClass.simpleName, "onChromecastConnecting")
                }

                override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
                    Log.d(javaClass.simpleName, "onChromecastConnected")
                    chromecastYouTubePlayerContext.initialize(object: AbstractYouTubePlayerListener() { // TODO funktioniert nicht on Reconnected
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            myYouTubePlayer = youTubePlayer
                        }

                    })
                }

                override fun onChromecastDisconnected() {
                    Log.d(javaClass.simpleName, "onChromecastDisconnected")
                }
            })
    }

    fun playVideo(videoId: String) {
        myYouTubePlayer?.loadVideo(videoId, 0f)
    }
}