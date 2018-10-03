package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.ChatUser

/**
 * Created by dangui on 13/11/17.
 */
@Dao
interface ChatDialogDao {

    @Query("SELECT * FROM ChatDialog ORDER BY lastMessageDate DESC, createdAt ASC")
    fun findAllChatDialogs(): LiveData<List<ChatDialog>>

    @Query("SELECT dialogId FROM ChatDialog")
    fun findAllChatDialogsId(): List<String>

    @Query("SELECT * FROM ChatDialog WHERE unreadMessagesCount > 0")
    fun findUnreadChatDialog(): LiveData<List<ChatDialog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatDialog: ChatDialog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatDialogs(chatDialogs: List<ChatDialog>)

    @Query("SELECT * FROM ChatDialog WHERE dialogId = :dialogId")
    fun findDialogWithId(dialogId: String): ChatDialog

    @Query("SELECT * FROM ChatDialog WHERE dialogId = :dialogId")
    fun findLiveDataDialogWithId(dialogId: String): LiveData<ChatDialog>

    @Query("SELECT * FROM ChatDialog WHERE type = 3")
    fun findAllPrivateDialogs() : List<ChatDialog>

    @Query("SELECT * FROM ChatDialog WHERE type = 2 ORDER BY lastMessageDate DESC")
    fun findAllGroupDialogs() : LiveData<List<ChatDialog>>

    @Query("DELETE FROM ChatDialog WHERE dialogId = :dialogId")
    fun deleteChatDialogById(dialogId: String)

    @Query("DELETE FROM ChatDialog")
    fun deleteAll(): Int

    @Query("DELETE FROM ChatDialog WHERE dialogId IN (:dialogsId)")
    fun deleteChatDialogByIds(dialogsId: Array<String>) : Int

    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatUsers(chatUsers: List<ChatUser>)
}