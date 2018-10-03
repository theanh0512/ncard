package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.Group

/**
 * Interface for database access for Filter related operations.
 */
@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(group: Group)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroups(groups: List<Group>)

    @Query("SELECT * FROM group_item")
    fun findAllGroups(): LiveData<List<Group>>

    @Query("DELETE FROM group_item WHERE id = :id")
    fun deleteGroupById(id: Int)

    @Query("DELETE FROM group_item")
    fun deleteAllGroups()

    @Update
    fun updateGroup(group: Group)
}