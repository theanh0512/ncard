package com.user.ncard.ui.chats.utils;

import android.support.annotation.Nullable;

import com.user.ncard.vo.ChatMessage;

/**
 * Basically in your app you should store users in database
 * And load users to memory on demand
 * We're using runtime SpaceArray holder just to simplify app logic
 */
public class ChatMessageActionHolder {

    private static ChatMessageActionHolder instance;

    private @Nullable
    ChatMessage chatMessage;
    private int type = -1;

    public static synchronized ChatMessageActionHolder getInstance() {
        if (instance == null) {
            instance = new ChatMessageActionHolder();
        }

        return instance;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }


    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
