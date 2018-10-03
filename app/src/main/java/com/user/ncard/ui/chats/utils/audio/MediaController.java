package com.user.ncard.ui.chats.utils.audio;

import android.net.Uri;

import com.user.ncard.ui.chats.views.QBPlaybackControlView;


/**
 * Created by Roman on 16.07.2017.
 */

public interface MediaController {

    interface EventMediaController {
        void onPlayerInViewInit(QBPlaybackControlView view);
    }

    void onPlayClicked(QBPlaybackControlView view, Uri uri);

    void onPauseClicked();

    void onStartPosition();

    void onStopAnyPlayback();
}
