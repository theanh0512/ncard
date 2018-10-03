package com.user.ncard.ui.chats.utils.audio;

import android.net.Uri;

import com.user.ncard.ui.chats.views.QBPlaybackControlView;


/**
 * Created by roman on 7/14/17.
 */

public interface MediaManager {

    void playMedia(QBPlaybackControlView playerView, Uri uri);

    void pauseMedia();

    void stopAnyPlayback();

    void resetMediaPlayer();
}