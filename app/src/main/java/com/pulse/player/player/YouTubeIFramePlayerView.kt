package com.pulse.player.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeIFramePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onPlaybackStateChange: (isPlaying: Boolean) -> Unit = {},
    onDurationChanged: (durationSeconds: Long) -> Unit = {}
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.BLACK)

                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                    }
                }

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onPlayerStateChange(state: Int) {
                        // 1 = playing, 2 = paused, 0 = ended
                        val isPlaying = (state == 1)
                        post { onPlaybackStateChange(isPlaying) }
                    }

                    @JavascriptInterface
                    fun onDuration(duration: Float) {
                        post { onDurationChanged(duration.toLong()) }
                    }
                }, "AndroidBridge")

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <style>
                            * { margin: 0; padding: 0; box-sizing: border-box; }
                            html, body { width: 100%; height: 100%; background-color: #000; overflow: hidden; }
                            #player { width: 100%; height: 100%; }
                        </style>
                    </head>
                    <body>
                        <div id="player"></div>
                        <script>
                            var tag = document.createElement('script');
                            tag.src = "https://www.youtube.com/iframe_api";
                            var firstScriptTag = document.getElementsByTagName('script')[0];
                            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                            var player;
                            function onYouTubeIframeAPIReady() {
                                player = new YT.Player('player', {
                                    height: '100%',
                                    width: '100%',
                                    videoId: '$videoId',
                                    playerVars: {
                                        'playsinline': 1,
                                        'autoplay': 1,
                                        'controls': 1,
                                        'rel': 0,
                                        'modestbranding': 1,
                                        'fs': 1
                                    },
                                    events: {
                                        'onReady': onPlayerReady,
                                        'onStateChange': onPlayerStateChange
                                    }
                                });
                            }

                            function onPlayerReady(event) {
                                event.target.playVideo();
                                if (window.AndroidBridge && player.getDuration) {
                                    window.AndroidBridge.onDuration(player.getDuration());
                                }
                            }

                            function onPlayerStateChange(event) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onPlayerStateChange(event.data);
                                }
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            // Update if videoId changes
        }
    )
}
