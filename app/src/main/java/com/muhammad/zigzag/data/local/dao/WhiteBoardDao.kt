package com.muhammad.zigzag.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.muhammad.zigzag.data.local.entity.WhiteBoardEntity

@Dao
interface WhiteBoardDao{
    @Insert
    suspend fun insertWhiteboard(whiteboard : WhiteBoardEntity) : Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWhiteboard(whiteboard: WhiteBoardEntity)
    @Query("SELECT * FROM whiteboard_table ORDER BY lastEdited DESC")
    fun getAllWhiteBoards() : Flow<List<WhiteBoardEntity>>
    @Query("DELETE FROM whiteboard_table WHERE id =:id")
    fun deleteWhiteboardById(id : Long)
    @Query("SELECT * FROM whiteboard_table WHERE id =:id")
    suspend fun getWhiteBoardById(id : Long) : WhiteBoardEntity?
    @Query("UPDATE whiteboard_table SET name =:name WHERE id =:id")
    suspend fun updateName(id : Long, name : String)
}