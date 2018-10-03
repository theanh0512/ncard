package com.user.ncard.ui.chats.views;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by trong-android-dev on 18/12/17.
 */

public class GoEditText extends android.support.v7.widget.AppCompatEditText {

    public interface GoEditTextListener {

        void onUpdate();

        boolean processConsume(boolean consume);
    }

    ArrayList<GoEditTextListener> listeners;

    public GoEditText(Context context) {
        super(context);
        listeners = new ArrayList<>();
    }

    public GoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        listeners = new ArrayList<>();
    }

    public GoEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        listeners = new ArrayList<>();
    }

    public void addListener(GoEditTextListener listener) {
        try {
            listeners.add(listener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Here you can catch paste, copy and cut events
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id) {
            case android.R.id.cut:
                onTextCut();
                break;
            case android.R.id.paste:
                onTextPaste();
                for (GoEditTextListener listener : listeners) {
                    return listener.processConsume(consumed);
                }
                break;
            case android.R.id.copy:
                onTextCopy();
        }
        return consumed;
    }

    public void onTextCut() {
    }

    public void onTextCopy() {
    }

    /**
     * adding listener for Paste for example
     */
    public void onTextPaste() {
        for (GoEditTextListener listener : listeners) {
            listener.onUpdate();
        }
    }
}
