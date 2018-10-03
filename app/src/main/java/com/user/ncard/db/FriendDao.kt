package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.Friend

/**
 * Interface for database access for User related operations.
 */
@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: Friend)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(friends: List<Friend>)

    @Query("SELECT * FROM friend ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun findAllFriends(): LiveData<List<Friend>>

    @Query("SELECT * FROM friend")
    fun loadAllFriends(): List<Friend>

    @Query("SELECT * FROM friend where chatId=:chatId")
    fun findFriendsByChatId(chatId: Int): Friend

    @Query("SELECT * FROM friend where id in (:ids)")
    fun findFriendsById(ids: List<Int>): LiveData<List<Friend>>

    @Query("SELECT * FROM friend WHERE (firstName LIKE :query OR lastName LIKE :query OR remark LIKE :query OR email LIKE :query) AND (nationality IN(:nationalities) AND" +
            " country IN(:countries) AND" +
            " gender IN(:genders)) COLLATE NOCASE" +
            " ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun findFriendsWithFilter(query: String, nationalities: List<String>, countries: List<String>, genders: List<String>): LiveData<List<Friend>>

    @Query("SELECT * FROM friend WHERE firstName LIKE :query OR lastName LIKE :query OR remark LIKE :query OR email LIKE :query COLLATE NOCASE" +
            " ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun findFriendsWithoutFilter(query: String): LiveData<List<Friend>>

    @Update
    fun updateFriend(friend: Friend)

    @Delete
    fun deleteFriend(friend: Friend)

    @Query("DELETE FROM friend WHERE id = :id")
    fun deleteFriendById(id: Int)

    @Query("DELETE FROM friend")
    fun deleteAllFriends():Int
}