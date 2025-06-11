package com.muhammad.zigzag.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.muhammad.zigzag.data.local.convertor.LocalDateConvertor
import com.muhammad.zigzag.data.local.convertor.PathConvertor
import com.muhammad.zigzag.data.local.dao.PathDao
import com.muhammad.zigzag.data.local.dao.WhiteBoardDao
import com.muhammad.zigzag.data.local.entity.PathEntity
import com.muhammad.zigzag.data.local.entity.WhiteBoardEntity

@Database(
    entities = [PathEntity::class, WhiteBoardEntity::class], version = 6, exportSchema = true
)
@TypeConverters(PathConvertor::class, LocalDateConvertor::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun pathDao() : PathDao
    abstract fun whiteBoardDao() : WhiteBoardDao
}