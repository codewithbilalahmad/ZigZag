package com.muhammad.zigzag.domain.repository

import kotlinx.coroutines.flow.Flow
import com.muhammad.zigzag.domain.model.DrawnPath

interface PathRepository {
    suspend fun upsertPath(path : DrawnPath)
    suspend fun deletePath(path : DrawnPath)
    fun getPathsForWhiteboard(whiteboardId : Long) : Flow<List<DrawnPath>>
}