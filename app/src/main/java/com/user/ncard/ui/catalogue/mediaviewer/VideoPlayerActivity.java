package com.user.ncard.ui.catalogue.mediaviewer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.user.ncard.R;


public class VideoPlayerActivity extends Activity implements OnPreparedListener, OnErrorListener {
    public static final String EXTRA_URL_VIDEO = "EXTRA_URL_VIDEO";

    public static Intent getIntent(Context context, String urlVideo) {
        return new Intent(context, VideoPlayerActivity.class).putExtra(EXTRA_URL_VIDEO, urlVideo);
    }

    protected VideoView videoView;

    protected String urlVideo;
    protected boolean pausedInOnStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);

        retrieveExtras();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.isPlaying()) {
            pausedInOnStop = true;
            videoView.pause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (pausedInOnStop) {
            videoView.start();
            pausedInOnStop = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * Retrieves the extra associated with the selected playlist index
     * so that we can start playing the correct item.
     */
    protected void retrieveExtras() {
        Bundle extras = getIntent().getExtras();
        urlVideo = extras.getString(EXTRA_URL_VIDEO);
    }

    protected void init() {
        videoView = (VideoView) findViewById(R.id.video_play_activity_video_view);
        videoView.setOnPreparedListener(this);
        videoView.setOnErrorListener(this);

        videoView.setVideoURI(Uri.parse(urlVideo));
    }

    protected void showErrorMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Playback Error")
                .setMessage(String.format("There was an error playing \"%s\"", "video"))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onPrepared() {
        videoView.start();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onError(Exception e) {
        showErrorMessage();
        return false;

    }
}
