package com.muhammad.zigzag.domain.repository

import kotlinx.coroutines.flow.Flow
import com.muhammad.zigzag.domain.model.WhiteBoard

interface WhiteBoardRepository {
    fun getAllWhiteBoards() : Flow<List<WhiteBoard>>
    suspend fun upsertWhiteboard(whiteBoard: WhiteBoard) : Long
    suspend fun getWhiteBoardById(id : Long) : WhiteBoard?
    suspend fun deleteWhiteboardById(id : Long)
    suspend fun updateWhiteboardName(id : Long, name : String)
}