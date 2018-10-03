package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.NCardInfo
import com.user.ncard.vo.User

/**
 * Interface for database access for User related operations.
 */
@Dao
interface NCardInfoDao {
    // Get data info
    @Query("SELECT * FROM NCardInfo")
    fun getNCardInfo(): LiveData<List<NCardInfo>>

    @Query("SELECT * FROM NCardInfo limit 1")
    fun getNCardInfoList(): List<NCardInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ncardInfo: NCardInfo)

    @Query("DELETE FROM NCardInfo")
    fun deleteAllNCardInfoDB(): Int

}