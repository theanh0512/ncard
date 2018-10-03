package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.user.ncard.vo.*

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Dao
interface BroadcastGroupDao {

    /*Insert*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBroadcastGroupChat(item: BroadcastGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBroadcastGroupChat(items: List<BroadcastGroup>)

    /*Query*/
    @Query("SELECT * FROM BroadcastGroup")
    fun getAllBroadcastGroupChat(): LiveData<List<BroadcastGroup>>

    @Query("SELECT * FROM BroadcastGroup WHERE id=:id")
    fun getBroadcastGroup(id: Int): LiveData<BroadcastGroup>

    /*Delete*/
    @Query("DELETE FROM BroadcastGroup")
    fun deleteAllBroadcastGroup(): Int

    @Query("DELETE FROM BroadcastGroup WHERE id =:id")
    fun deleteBroadcastGroup(id: Int): Int


}