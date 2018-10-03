/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.user.ncard.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.user.ncard.vo.NCardInfo
import com.user.ncard.vo.User


/**
 * Interface for database access for User related operations.
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<User>)

    @Query("SELECT * FROM user WHERE email = :username")
    fun findByUsername(username: String): LiveData<User>

    @Query("SELECT * FROM user WHERE id in (:userIds)")
    fun findByUserIds(userIds: List<Int>): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id = :userId")
    fun findByUserId(userId: Int): LiveData<User>

    @Query("SELECT * FROM user WHERE id = :userId")
    fun loadByUserId(userId: Int): User

    @Update
    fun updateUser(user:User)

    @Query("DELETE FROM User")
    fun deleteAllUser(): Int
}
