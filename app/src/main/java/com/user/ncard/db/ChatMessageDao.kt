package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.user.ncard.vo.ChatMessage

/**
 * Created by dangui on 13/11/17.
 */
@Dao
interface ChatMessageDao {

    //load newest messages with limit from
    @Query("SELECT * FROM ChatMessage WHERE (dialogId = :dialogId) and (system_data IS NULL or system_data = '') ORDER BY createdAt DESC LIMIT :limit")
    fun findChatMessagesOfDialog(dialogId: String, limit: Int): LiveData<List<ChatMessage>>

    @Query("SELECT * FROM ChatMessage WHERE (dialogId = :dialogId) and (system_data IS NULL or system_data = '') ORDER BY createdAt DESC LIMIT 1")
    fun findLastChatMessagesOfDialog(dialogId: String): List<ChatMessage>

    //load newest messages with limit from
    @Query("SELECT * FROM ChatMessage WHERE dialogId = :dialogId")
    fun findChatMessages(dialogId: String): List<ChatMessage>

    @Query("SELECT * FROM ChatMessage WHERE messageId = :messageId")
    fun findChatMessage(messageId: String): ChatMessage

    //load more messages when reach the top of chat screen
    @Query("SELECT * FROM ChatMessage WHERE dialogId = :dialogId AND createdAt < :timestamp ORDER BY createdAt DESC LIMIT :limit")
    fun findChatMessagesBefore(dialogId: String, timestamp: Long, limit: Int): LiveData<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatMessage: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessages(chatMessages: List<ChatMessage>)

    @Query("DELETE FROM ChatMessage WHERE dialogId = :dialogId")
    fun deleteMessagesFrom(dialogId: String) : Int

    @Query("DELETE FROM ChatMessage WHERE messageId = :messageId")
    fun deleteMessageId(messageId: String) : Int

    @Query("DELETE FROM ChatMessage")
    fun deleteAll() : Int

    @Query("DELETE FROM ChatMessage WHERE messageId IN (:ids)")
    fun deleteChatMessages(ids: Array<String>) : Int
}