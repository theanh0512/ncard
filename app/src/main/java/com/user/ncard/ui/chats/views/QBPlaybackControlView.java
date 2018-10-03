package com.user.ncard.ui.chats.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.user.ncard.R;
import com.user.ncard.ui.catalogue.utils.Functions;
import com.user.ncard.ui.chats.utils.audio.AudioController;
import com.user.ncard.ui.chats.utils.audio.ExoPlayerEventListenerImpl;

/**
 * Created by roman on 8/1/17.
 */

public class QBPlaybackControlView extends LinearLayout {
    private static String TAG = QBPlaybackControlView.class.getSimpleName();

    private static final int[] STATE_SET_PLAY =
            {R.attr.state_play, -R.attr.state_pause};
    private static final int[] STATE_SET_PAUSE =
            {-R.attr.state_play, R.attr.state_pause};

    private final TextView durationView;
    private final TextView positionView;

    /*private final View playButton;
    private final View pauseButton;*/
    private final ImageView iconPlayPauseView;

    private final ComponentListener componentListener;
    private AudioController mediaController;
    private Uri uri;
    ExoPlayer player;
    private Handler handler;

    public QBPlaybackControlView(Context context) {
        this(context, null);
    }

    public QBPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QBPlaybackControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        componentListener = new ComponentListener();

        int audioControllerLayoutId = R.layout.audio_playback_control_view_left;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.PlaybackControlView, 0, 0);
        try {
            audioControllerLayoutId = a.getResourceId(R.styleable.PlaybackControlView_audio_controller_layout_id,
                    audioControllerLayoutId);
        } finally {
            a.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(audioControllerLayoutId, this, true);

        durationView = (TextView) findViewById(R.id.tvTimeDuration);
        positionView = (TextView) findViewById(R.id.tvTimePosition);
        iconPlayPauseView = (ImageView) findViewById(R.id.imvPlayPause);
        if (iconPlayPauseView != null) {
            iconPlayPauseView.setOnClickListener(componentListener);
        }
        alwaysShow();
    }


    public ExoPlayer getPlayer() {
        return player;
    }

    private void alwaysShow() {
        setVisibility(VISIBLE);
    }

    public void setDurationViewOnTop() {
        setDurationViewVisibility(VISIBLE);
        setPositionViewVisibility(GONE);
    }

    public void setPositionViewOnTop() {
        setPositionViewVisibility(VISIBLE);
        setDurationViewVisibility(GONE);
    }

    private void setPositionViewVisibility(int visibility) {
        positionView.setVisibility(visibility);
    }

    private void setDurationViewVisibility(int visibility) {
        durationView.setVisibility(visibility);
    }

    public void initView(AudioController mediaController, Uri uri) {
        initMediaController(mediaController);
        setUri(uri);
    }

    private void initMediaController(AudioController mediaController) {
        this.mediaController = mediaController;
    }

    private void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setPlayer(ExoPlayer player) {
        if (getPlayer() == player) {
            return;
        }
        if (getPlayer() != null) {
            getPlayer().removeListener(componentListener);
        }
        if (player != null) {
            this.player = player;
            player.addListener(componentListener);
        }
    }

    public void restoreState(ExoPlayer player) {
        if (player != null) {
            setPlayer(player);
            updatePositionDurationViews();
            updateViewState();
        }
    }

    private void updatePositionDurationViews() {
        if (getPlayer().getCurrentPosition() == 0) {
            setDurationViewOnTop();
        } else {
            setPositionViewOnTop();
        }
    }

    private void updateViewState() {
        if (getPlayer().getPlaybackState() == ExoPlayer.STATE_ENDED) {
            resetPlayerPosition();
        } else {
            updatePlayPauseIconView();
        }
    }

    public void releaseView() {
        Log.d(TAG, "releaseView");
        setDurationViewOnTop();
        disposeViewPlayer();
    }

    public void disposeViewPlayer() {
        Log.d(TAG, "disposeViewPlayer");
        if (this.getPlayer() != null) {
            setPlayer(null);
            updatePlayPauseIconView();
        }
    }

    public boolean isCurrentViewPlaying() {
        return getPlayer() != null;
    }

    public void updatePlayPauseIconView() {
        if (getVisibility() == VISIBLE || iconPlayPauseView == null) {
            return;
        }
        boolean requestPlayPauseFocus;
        requestPlayPauseFocus = getPlayer() != null && getPlayer().getPlayWhenReady();

        if (requestPlayPauseFocus) {
            setPauseStateIcon();
        } else {
            setPlayStateIcon();
        }
    }

    private void setPlayStateIcon() {
        iconPlayPauseView.setActivated(false);
        iconPlayPauseView.setImageState(STATE_SET_PLAY, true);
    }

    private void setPauseStateIcon() {
        iconPlayPauseView.setActivated(true);
        iconPlayPauseView.setImageState(STATE_SET_PAUSE, true);
    }

    class ComponentListener extends ExoPlayerEventListenerImpl implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            /*if (playButton == view) {
                performPlayClick();
            } else if (pauseButton == view) {
                performPauseClick();
            }*/
            clickIconPlayPauseView();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED && playWhenReady) {
                resetPlayerPosition();
                clickIconPlayPauseView();
                // Cancel handler
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
            } else if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
                setProgress();
            }
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            super.onTracksChanged(trackGroups, trackSelections);
        }
    }

    private void setProgress() {
        if (handler == null) handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getPlayer() != null) {
                    positionView.setText(Functions.stringForTime((int) (getPlayer().getCurrentPosition())));

                    handler.postDelayed(this, 1000);
                }
            }
        });
    }


    private void resetPlayerPosition() {
        mediaController.onStartPosition();
        setDurationViewOnTop();
    }

    public void clickIconPlayPauseView() {
        if (iconPlayPauseView != null) {
            if (!iconPlayPauseView.isActivated()) {
                iconPlayPauseView.setImageState(STATE_SET_PAUSE, true);
                performPlayClick();
            } else {
                iconPlayPauseView.setImageState(STATE_SET_PLAY, true);
                performPauseClick();
            }
            iconPlayPauseView.setActivated(!iconPlayPauseView.isActivated());
        }
    }

    private void performPlayClick() {
        setPositionViewOnTop();
        mediaController.onPlayClicked(QBPlaybackControlView.this, uri);
    }

    private void performPauseClick() {
        //mediaController.onPauseClicked();
        resetPlayerPosition();
        //clickIconPlayPauseView();
        // Cancel handler
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        mediaController.onStopAnyPlayback();
    }
}