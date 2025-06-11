package com.muhammad.zigzag.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.muhammad.zigzag.data.local.entity.PathEntity

@Dao
interface PathDao {
    @Upsert
    suspend fun upsertPath(pathEntity: PathEntity)
    @Delete
    suspend fun deletePath(pathEntity: PathEntity)
    @Query("SELECT * FROM pathTable WHERE whiteBoardId =:whiteBoardId")
    fun getPathsForWhiteboard(whiteBoardId : Long) : Flow<List<PathEntity>>
}