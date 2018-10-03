package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.Job

/**
 * Interface for database access for Filter related operations.
 */
@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(job: Job)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobs(jobs: List<Job>)

    @Query("SELECT * FROM job")
    fun findAllJobs(): LiveData<List<Job>>

    @Query("DELETE FROM job WHERE id = :id")
    fun deleteJobById(id: Int)

    @Query("DELETE FROM job")
    fun deleteAllJobs()

    @Update
    fun updateJob(job: Job)
}