package com.user.ncard.ui.me.ewallet;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

/**
 * An implementation of {@link EphemeralKeyProvider} that can be used to generate
 * ephemeral keys on the backend.
 */
public class NCardEphemeralKeyProvider implements EphemeralKeyProvider {


    private String key;

    public NCardEphemeralKeyProvider(String key) {
        this.key = key;
    }

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
                                   @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {
        keyUpdateListener.onKeyUpdate(key);
    }
}
