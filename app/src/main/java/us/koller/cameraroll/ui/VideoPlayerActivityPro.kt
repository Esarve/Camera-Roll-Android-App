package us.koller.cameraroll.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import us.koller.cameraroll.R
import us.koller.cameraroll.databinding.ActivityVideoPlayerProBinding

class VideoPlayerActivityPro: ThemeableActivity() {

    private lateinit var binding: ViewBinding
    private lateinit var exoplayer: ExoPlayer
    private lateinit var playerview: StyledPlayerView
    private lateinit var videoUri: Uri;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerProBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoUri = intent.data!!
        exoplayer = ExoPlayer.Builder(this).build()

        val mediaItem = MediaItem.fromUri(videoUri);

        playerview = findViewById(R.id.exoplayer2)
        playerview.player = exoplayer

        playerview.setControllerVisibilityListener {
            if (it != View.VISIBLE) {
                //make view visible again, so the Animation is visible
                playerview.visibility = View.VISIBLE;

                // Other things might appear later
            }
        }

        exoplayer.setMediaItem(mediaItem)
        exoplayer.prepare()
        exoplayer.play()
    }

}