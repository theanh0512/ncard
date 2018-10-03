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


import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import com.user.ncard.util.Converter
import com.user.ncard.util.ListStringConverter
import com.user.ncard.util.ListTagConverter
import com.user.ncard.vo.*
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.User

/**
 * Main database description.
 */
@Database(entities = arrayOf(User::class, Friend::class, Filter::class, TagPost::class, TagGroupPost::class,
        CatalogueScreenTracking::class, CataloguePostScreenTracking::class,
        CataloguePost::class, CatalogueComment::class, CatalogueLike::class, NameCard::class, Group::class,
        ChatDialog::class, ChatMessage::class, ChatUser::class, Job::class,
        BroadcastGroup::class, NCardInfo::class), version = 3)
@TypeConverters(Converter::class, ListStringConverter::class, ListTagConverter::class)
abstract class NcardDb : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao
    abstract fun filterDao(): FilterDao
    abstract fun catalogueDao(): CatalogueDao
    abstract fun nameCardDao(): NameCardDao
    abstract fun groupDao(): GroupDao
    abstract fun chatDialogDao(): ChatDialogDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun jobDao(): JobDao
    abstract fun broadcastGroupDao(): BroadcastGroupDao
    abstract fun nCardInfoDao(): NCardInfoDao

    companion object {
        // Version 1.0.5 - 1.0.6
        @JvmStatic
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase?) {
                database?.execSQL("ALTER TABLE Job " + " ADD COLUMN card_id INTEGER")
            }
        }
        // Version 1.0.8 - 1.0.9
        @JvmStatic
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase?) {
                //database?.execSQL("DROP TABLE `filter`; CREATE TABLE IF NOT EXISTS `filter` (`name` TEXT NOT NULL, `type` TEXT NOT NULL, `parent` TEXT NOT NULL, PRIMARY KEY(`name`, `type`));")
                // Create the new table
                database?.execSQL("CREATE TABLE filter_copy (`name` TEXT NOT NULL, `type` TEXT NOT NULL, `parent` TEXT NOT NULL, PRIMARY KEY(`name`, `type`))")
                database?.execSQL("INSERT INTO filter_copy (name, type, parent) SELECT name, type, parent FROM filter")
                database?.execSQL("DROP TABLE filter")
                database?.execSQL("ALTER TABLE filter_copy RENAME TO filter")
            }
        }
    }
}
