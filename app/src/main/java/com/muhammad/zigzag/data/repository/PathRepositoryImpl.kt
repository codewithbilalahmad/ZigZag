package com.muhammad.zigzag.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.muhammad.zigzag.data.local.dao.PathDao
import com.muhammad.zigzag.data.mapper.toDrawnPaths
import com.muhammad.zigzag.data.mapper.toPathEntity
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.domain.repository.PathRepository

class PathRepositoryImpl(private val pathDao : PathDao): PathRepository{
    override suspend fun upsertPath(path: DrawnPath) {
        pathDao.upsertPath(path.toPathEntity())
    }

    override suspend fun deletePath(path: DrawnPath) {
        pathDao.deletePath(path.toPathEntity())
    }

    override fun getPathsForWhiteboard(whiteboardId: Long): Flow<List<DrawnPath>> {
        return pathDao.getPathsForWhiteboard(whiteboardId).map { it.toDrawnPaths() }
    }
}