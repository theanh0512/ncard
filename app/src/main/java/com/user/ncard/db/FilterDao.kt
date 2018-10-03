package com.user.ncard.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.user.ncard.vo.Filter

/**
 * Interface for database access for Filter related operations.
 */
@Dao
interface FilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filter: Filter)

    @Query("SELECT * from filter where type = 'industry'")
    fun getAllIndustryFilters(): List<Filter>?

    @Query("SELECT * from filter where type = 'country'")
    fun getAllCountryFilters(): List<Filter>?

    @Query("SELECT * from filter where type = 'nationality'")
    fun getAllNationalityFilters(): List<Filter>?

    @Query("DELETE FROM filter where parent = 'friend'")
    fun deleteAllFiltersOfFriend()

    @Query("DELETE FROM filter where parent = 'namecard'")
    fun deleteAllFiltersOfNameCard()
}