package com.flauschcode.omcast

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.cast.framework.CastButtonFactory
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


class MainActivity : AppCompatActivity() {

    // TODO stop playing when choosing to cast

    private val googlePlayServicesAvailabilityRequestCode = 1
    private var chromecastBridge: ChromecastBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO
        val playlists = listOf(
            "PLdJZKgGi4pKDRGaIYi5KjblA21C_qZyw1", // Yin Yoga
            "PLOhPBXxBh2qBjv9dAzWreaoXwbxpRVALd", // Inner Garden Meditations
            "PLui6Eyny-Uzyp5P3Vcuv5qCHQOC8W6grN", // YWA Move
            "PLui6Eyny-UzzJ4NSTesh4xRWg4ZWNz5s4", // YWA Breath
            "PLui6Eyny-UzzFFfpiil94CUrWKVMaqmkm", // YWA Home
            "PLui6Eyny-UzzkcCfrpXcgUS0wfEGA-kej", // YWA Dedicate
            "PLui6Eyny-UzwIo3OBXV_KlsWaxUANvWhh" // YWA True
        )

        setContent {
            AppCompatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            OmCastScreen(playlists = playlists, navController = navController)
                        }
                        composable("fullscreen/{playlistId}") { backStackEntry ->
                            FullscreenVideo(playlistId = backStackEntry.arguments?.getString("playlistId")) {
                                    videoId -> chromecastBridge?.playVideo(videoId) }
                        }
                    }

                }
            }
        }

        PlayServicesUtils.checkGooglePlayServicesAvailability(
            this,
            googlePlayServicesAvailabilityRequestCode
        ) {
            chromecastBridge = ChromecastBridge(this)
        }
    }

}

@Composable
fun OmCastScreen(
    playlists: List<String>,
    navController: NavHostController
){
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    MyMediaRouteButton()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
        ) {
            items(playlists) { playlist ->
                MyYouTubePlayerView(playlistId = playlist, onVideoClicked = { navController.navigate("fullscreen/$playlist")})
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyYouTubePlayerView(
    playlistId: String,
    onVideoClicked: (playlistId: String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = Modifier.pointerInteropFilter { event ->
            if (event.action == MotionEvent.ACTION_UP) onVideoClicked(playlistId)
            true
        },
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false

                val iFramePlayerOptions: IFramePlayerOptions = IFramePlayerOptions.Builder()
                    .controls(0)
                    .listType("playlist")
                    .list(playlistId)
                    .build()

                lifecycleOwner.lifecycle.addObserver(this)

                initialize(object : AbstractYouTubePlayerListener() {}, true, iFramePlayerOptions)
            }
        }
    )
}

@Composable
fun FullscreenVideo(
    playlistId: String?,
    onVideoSelected: (videoId: String) -> Unit,
) {
    val activity = LocalContext.current as Activity // TODO LocalConfiguration?
    val lifecycleOwner = LocalLifecycleOwner.current

    val systemUiController = rememberSystemUiController()

    DisposableEffect(Unit) {
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        systemUiController.isSystemBarsVisible = false // Status & Navigation bars
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
            systemUiController.isSystemBarsVisible = true // Status & Navigation bars
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        AndroidView(
            factory = { context ->
                YouTubePlayerView(context).apply {
                    enableAutomaticInitialization = false

                    val iFramePlayerOptions: IFramePlayerOptions = IFramePlayerOptions.Builder()
                        .controls(0)
                        .listType("playlist")
                        .list(playlistId!!) // TODO ??
                        .build()

                    lifecycleOwner.lifecycle.addObserver(this)

                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            //enterFullScreen()
                        }

                        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                            onVideoSelected(videoId)
                        }
                    }, true, iFramePlayerOptions)
                }
            }
        )
    }
}

@Composable
fun MyMediaRouteButton() {
    AndroidView(factory = { context -> MediaRouteButton(context).apply {
        CastButtonFactory.setUpMediaRouteButton(context, this)
    } })
}