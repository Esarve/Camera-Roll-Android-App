package us.koller.cameraroll.ui

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import us.koller.cameraroll.R
import us.koller.cameraroll.util.Util

class VideoPlayerActivity : ThemeableActivity() {
    private var videoUri: Uri? = null
    private var player: SimpleExoPlayer? = null
    private var playerPosition: Long = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val intent = intent
        videoUri = intent.data
        if (videoUri == null) {
            return
        }

        //needed to achieve transparent navBar
        showOrHideSystemUi(true)

        //init Play pause button
        val playPause = findViewById<ImageButton>(R.id.play_pause)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playPause.setImageResource(R.drawable.pause_to_play_avd)
        } else {
            playPause.setImageResource(R.drawable.ic_pause_white)
        }
        playPause.setOnClickListener {
            if (player != null) {
                player!!.playWhenReady = !player!!.playWhenReady
            }
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = null
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setWindowInsets()

        //hide & show Nav-/StatusBar together with controls
        val playbackControlView = findViewById<PlayerControlView>(R.id.playback_control_view)
        val bottomBarControls = findViewById<View>(R.id.controls)
        //        playbackControlView.setVisibilityListener(
//                new PlaybackControlView.VisibilityListener() {
//                    @Override
//                    public void onVisibilityChange(final int i) {
//                        //animate Toolbar & controls
//                        if (i != View.VISIBLE) {
//                            //make view visible again, so the Animation is visible
//                            playbackControlView.setVisibility(View.VISIBLE);
//                        }
//
//                        float toolbar_translationY = i == View.VISIBLE ? 0
//                                : -(toolbar.getHeight());
//                        toolbar.animate()
//                                .translationY(toolbar_translationY)
//                                .setInterpolator(new AccelerateDecelerateInterpolator())
//                                .setListener(new AnimatorListenerAdapter() {
//                                    @Override
//                                    public void onAnimationEnd(Animator animation) {
//                                        super.onAnimationEnd(animation);
//                                        playbackControlView.setVisibility(i);
//                                    }
//                                })
//                                .start();
//
//                        float controls_translationY = i == View.VISIBLE ? 0
//                                : bottomBarControls.getHeight();
//                        bottomBarControls.animate()
//                                .translationY(controls_translationY)
//                                .setInterpolator(new AccelerateDecelerateInterpolator())
//                                .start();
//
//                        //show/hide Nav-/StatusBar
//                        showOrHideSystemUi(i == View.VISIBLE);
//                    }
//                });
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setWindowInsets()
    }

    fun setWindowInsets() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val bottomBarControls = findViewById<View>(R.id.controls)
        val rootView = findViewById<ViewGroup>(R.id.root_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            rootView.setOnApplyWindowInsetsListener { view, insets ->
                toolbar.setPadding(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, 0
                )
                bottomBarControls.setPadding(
                    insets.systemWindowInsetLeft,
                    0, insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom
                )

                // clear this listener so insets aren't re-applied
                rootView.setOnApplyWindowInsetsListener(null)
                insets.consumeSystemWindowInsets()
            }
        } else {
            rootView.viewTreeObserver
                .addOnGlobalLayoutListener(
                    object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            //hacky way of getting window insets on pre-Lollipop
                            val screenSize = Util
                                .getScreenSize(this@VideoPlayerActivity)
                            val windowInsets = intArrayOf(
                                Math.abs(screenSize[0] - rootView.left),
                                Math.abs(screenSize[1] - rootView.top),
                                Math.abs(screenSize[2] - rootView.right),
                                Math.abs(screenSize[3] - rootView.bottom)
                            )
                            toolbar.setPadding(
                                windowInsets[0], windowInsets[1],
                                windowInsets[2], 0
                            )
                            bottomBarControls.setPadding(
                                windowInsets[0], 0,
                                windowInsets[2], windowInsets[3]
                            )
                            rootView.viewTreeObserver
                                .removeOnGlobalLayoutListener(this)
                        }
                    })
        }
    }

    fun showOrHideSystemUi(show: Boolean) {
        if (show) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> {}
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
        if (playerPosition != -1L) {
            player!!.seekTo(playerPosition)
        }
    }

    private fun initPlayer() {
//        // Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
//                Util.getUserAgent(this, getString(R.string.app_name)), null);
//        // Produces Extractor instances for parsing the media data.
//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource(videoUri,
//                dataSourceFactory, extractorsFactory, null, null);
//
//        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
//
//        // Create the player
//        player = ExoPlayerFactory.newSimpleInstance(renderersFactory,
//                new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(null)),
//                new DefaultLoadControl());
//
//        // Bind the player to the view.
//        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
//        simpleExoPlayerView.setPlayer(player);
//
//        // Prepare the player with the source.
//        player.prepare(videoSource);
//        player.setRepeatMode(Player.REPEAT_MODE_ONE);
//        player.setPlayWhenReady(true);
//
//        final ImageButton playPause = findViewById(R.id.play_pause);
//        player.addListener(new SimpleEventListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                //update PlayPause-Button
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && showAnimations()) {
//                    if (player.getPlayWhenReady()) {
//                        playPause.setImageResource(R.drawable.play_to_pause_avd);
//                    } else {
//                        playPause.setImageResource(R.drawable.pause_to_play_avd);
//                    }
//
//                    Drawable d = playPause.getDrawable();
//                    if (d instanceof Animatable) {
//                        ((Animatable) d).start();
//                    }
//                } else {
//                    if (player.getPlayWhenReady()) {
//                        playPause.setImageResource(R.drawable.ic_pause_white);
//                    } else {
//                        playPause.setImageResource(R.drawable.ic_play_arrow_white);
//                    }
//                }
//            }
//        });
    }

    override fun onPause() {
        super.onPause()
        if (player!!.playWhenReady && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .build()
            enterPictureInPictureMode(params)
        }
    }

//    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
////        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
////        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
////        if (isInPictureInPictureMode) {
////            // Hide the controls in picture-in-picture mode.
////            simpleExoPlayerView.hideController();
////        } else {
////            // Restore the playback UI based on the playback status.
////            simpleExoPlayerView.showController();
////        }
//    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            playerPosition = player!!.currentPosition
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}