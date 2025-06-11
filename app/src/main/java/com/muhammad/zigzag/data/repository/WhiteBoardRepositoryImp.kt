package com.muhammad.zigzag.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.muhammad.zigzag.data.local.dao.WhiteBoardDao
import com.muhammad.zigzag.data.mapper.toWhiteBoard
import com.muhammad.zigzag.data.mapper.toWhiteBoardEntity
import com.muhammad.zigzag.data.mapper.toWhiteBoardList
import com.muhammad.zigzag.domain.model.WhiteBoard
import com.muhammad.zigzag.domain.repository.WhiteBoardRepository

class WhiteBoardRepositoryImp(private val whiteBoardDao: WhiteBoardDao) : WhiteBoardRepository{
    override fun getAllWhiteBoards(): Flow<List<WhiteBoard>> {
        return whiteBoardDao.getAllWhiteBoards().map { it.toWhiteBoardList() }
    }

    override suspend fun upsertWhiteboard(whiteBoard: WhiteBoard): Long {
        return if(whiteBoard.id == null){
            whiteBoardDao.insertWhiteboard(whiteBoard.toWhiteBoardEntity())
        } else {
            whiteBoardDao.updateWhiteboard(whiteBoard.toWhiteBoardEntity())
            whiteBoard.id
        }
    }

    override suspend fun getWhiteBoardById(id: Long): WhiteBoard? {
        return whiteBoardDao.getWhiteBoardById(id)?.toWhiteBoard()
    }

    override suspend fun deleteWhiteboardById(id: Long) {
        whiteBoardDao.deleteWhiteboardById(id)
    }

    override suspend fun updateWhiteboardName(id: Long, name: String) {
        whiteBoardDao.updateName(id = id, name = name)
    }

}