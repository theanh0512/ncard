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
interface CatalogueDao {

    /*Insert*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(items: List<TagPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTagGroup(items: List<TagGroupPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCataloguePost(items: List<CataloguePost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCataloguePost(item: CataloguePost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogueLike(items: List<CatalogueLike>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogueComment(items: List<CatalogueComment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogueScreenTracking(item: CatalogueScreenTracking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCataloguePostScreenTracking(item: CataloguePostScreenTracking)
    /*Insert*/

    /*Query*/
    @Query("SELECT * FROM TagPost")
    fun getAllTags(): LiveData<List<TagPost>>

    @Query("SELECT * FROM TagGroupPost")
    fun getAllTagGroups(): LiveData<List<TagGroupPost>>

    @Query("SELECT * FROM CataloguePostScreenTracking")
    fun getAllCataloguePostScreenTracking(): List<CataloguePostScreenTracking>


    @Query("SELECT * FROM CataloguePost INNER JOIN CataloguePostScreenTracking " +
            "ON CataloguePost.id = CataloguePostScreenTracking.postId " +
            "WHERE CataloguePostScreenTracking.screenTrackingId=:screenTrackingId AND CataloguePost.postCategory=:category " +
            "ORDER BY id DESC")
    fun getAllCataloguePostContainers(screenTrackingId: Int, category: String): LiveData<List<CatalogueContainer>>

    @Query("SELECT * FROM CataloguePost INNER JOIN CataloguePostScreenTracking " +
            "ON CataloguePost.id = CataloguePostScreenTracking.postId " +
            "WHERE CataloguePostScreenTracking.screenTrackingId=:screenTrackingId " +
            "ORDER BY id DESC")
    fun getAllCataloguePosts(screenTrackingId: Int): LiveData<List<CataloguePost>>

    @Query("SELECT * , COUNT(postId) as postCount FROM CataloguePostScreenTracking WHERE screenTrackingId=:screenTrackingId " +
            "GROUP BY postId HAVING postCount = 1")
    fun getCataloguePostScreenTracking(screenTrackingId: Int): List<CataloguePostScreenTracking>

    @Query("SELECT * FROM CataloguePost WHERE id=:id")
    fun getCataloguePostContainer(id: Int): LiveData<CatalogueContainer>
    /*Query*/

    /*Delete*/
    @Query("DELETE FROM CataloguePost WHERE id IN (:catalogueIds)")
    fun deleteCataloguePost(catalogueIds: Array<Int>) : Int

    @Query("DELETE FROM CataloguePost WHERE id=:id")
    fun deleteCataloguePostById(id: Int) : Int

    @Query("DELETE FROM CataloguePost")
    fun deleteAllCatalogue(): Int

    @Query("DELETE FROM CataloguePostScreenTracking")
    fun deleteAllCataloguePostScreenTracking(): Int

    /*Delete*/

}