package com.example.telegramspam.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.AccountSettings
import com.example.telegramspam.models.InviterSettings
import com.example.telegramspam.models.JoinerSettings

@Database(
    entities = [Account::class, AccountSettings::class, JoinerSettings::class, InviterSettings::class],
    version = 19
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountsDao(): AccountsDao
    abstract fun accountSettingsDao(): AccountSettingsDao
    abstract fun joinerSettingsDao(): JoinerSettingsDao
    abstract fun inviterSettingsDao(): InviterSettingsDao
}