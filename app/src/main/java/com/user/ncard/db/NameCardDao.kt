package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.NameCard

/**
 * Interface for database access for NameCard related operations.
 */
@Dao
interface NameCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(nameCard: NameCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNameCards(nameCards: List<NameCard>)

    @Query("SELECT * FROM nameCard where jobId is null ORDER BY name COLLATE NOCASE ASC")
    fun findAllNameCards(): LiveData<List<NameCard>>

    @Query("SELECT * FROM nameCard where jobId is not null")
    fun findAllMyNameCards(): LiveData<List<NameCard>>

    @Query("SELECT * FROM nameCard where id in (:ids)")
    fun findNameCardsById(ids: List<Int>): LiveData<List<NameCard>>

    @Update
    fun updateNameCard(nameCard: NameCard)

    @Delete
    fun deleteNameCard(nameCard: NameCard)

    @Query("DELETE FROM nameCard WHERE id = :id")
    fun deleteNameCardById(id: Int)

    @Query("DELETE FROM nameCard where jobId is not null")
    fun deleteAllMyNameCards()

    @Query("DELETE FROM nameCard where jobId is null")
    fun deleteAllNameCards()

    @Query("DELETE FROM nameCard")
    fun deleteAllNameCardsDB(): Int

    @Query("SELECT * FROM nameCard WHERE (name LIKE :query OR title LIKE :query OR remark LIKE :query OR email LIKE :query OR company LIKE :query) AND (nationality IN(:nationalities) AND" +
            " country IN(:countries) AND industry IN(:industries) AND" +
            " gender IN(:genders)) AND jobId is null COLLATE NOCASE" +
            " ORDER BY name COLLATE NOCASE ASC")
    fun findNameCardsWithFilter(query: String, nationalities: List<String>, countries: List<String>, genders: List<String>, industries: List<String>): LiveData<List<NameCard>>

    @Query("SELECT * FROM nameCard WHERE (name LIKE :query OR title LIKE :query OR remark LIKE :query OR email LIKE :query OR company LIKE :query) AND jobId is null COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    fun findNameCardsWithoutFilter(query: String): LiveData<List<NameCard>>
}