package com.muhammad.zigzag.data.local

import android.content.Context
import androidx.room.Room
import com.muhammad.zigzag.utils.Constants.APP_DATABASE_NAME
import kotlinx.coroutines.Dispatchers

fun getRoomDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(context,APP_DATABASE_NAME)
        .fallbackToDestructiveMigration(dropAllTables = true).setQueryCoroutineContext(
        Dispatchers.IO
    ).build()
}