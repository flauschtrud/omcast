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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.mediarouter.app.MediaRouteButton
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
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

        setContent {
            AppCompatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()

                    var playlists by rememberPreference(key = PLAYLIST_IDS, defaultValue = "")
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                           OmCastScreen(playlists = playlists.split("\n"), navController = navController)

                        }
                        composable("fullscreen/{playlistId}") { backStackEntry ->
                            FullscreenVideo(playlistId = backStackEntry.arguments?.getString("playlistId")) {
                                    videoId -> chromecastBridge?.playVideo(videoId) }
                        }
                        dialog(route = "settings",
                            dialogProperties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            UserPreferencesDialog(
                                playlists,
                                { playlists = it },
                                { navController.popBackStack() }
                            )
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

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, stringResource(id = R.string.settings))
                    }
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
    key(playlistId) { // makes sure that a new YouTubePlayerView is created on recomposition
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

        // currently there doesn't seem to be a more compose-specific solution
        // https://stackoverflow.com/questions/73063646/how-to-best-change-systembarsbehavior-with-jetpack-compose
        with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

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
                            enterFullScreen()
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