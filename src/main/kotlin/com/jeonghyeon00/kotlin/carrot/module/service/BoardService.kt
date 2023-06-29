package com.jeonghyeon00.kotlin.carrot.module.service

import com.jeonghyeon00.kotlin.carrot.module.dto.boardDto.BoardReq
import com.jeonghyeon00.kotlin.carrot.module.dto.boardDto.BoardReq.Companion.toBoard
import com.jeonghyeon00.kotlin.carrot.module.dto.boardDto.BoardPageRes
import com.jeonghyeon00.kotlin.carrot.module.dto.boardDto.BoardRes
import com.jeonghyeon00.kotlin.carrot.module.dto.imageDto.ImageReq.Companion.toImage
import com.jeonghyeon00.kotlin.carrot.module.entity.Board
import com.jeonghyeon00.kotlin.carrot.module.global.exception.BaseException
import com.jeonghyeon00.kotlin.carrot.module.global.exception.BaseExceptionCode
import com.jeonghyeon00.kotlin.carrot.module.repository.BoardRepository
import com.jeonghyeon00.kotlin.carrot.module.repository.ImageRepository
import com.jeonghyeon00.kotlin.carrot.module.repository.RegionRepository
import com.jeonghyeon00.kotlin.carrot.module.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val regionRepository: RegionRepository,
    private val imageRepository: ImageRepository,
) {
    @Transactional
    fun postBoard(userId: String, regionNumber: Int, boardReq: BoardReq): Board {
        val user = userRepository.findByIdOrNull(userId) ?: throw BaseException(BaseExceptionCode.USER_NOT_FOUND)
        val board = boardRepository.save(boardReq.toBoard(user, user.regions[regionNumber]))
        imageRepository.saveAll(
            boardReq.images.map {
                it.toImage(board)
            },
        )
        return board
    }

    @Transactional
    fun getBoards(pageable: Pageable): Page<BoardPageRes> {
        return boardRepository.findAll(pageable).map {
            BoardPageRes.toBoardPageRes(it)
        }
    }

    @Transactional
    fun deleteBoard(userId: String, boardId: Long): Boolean {
        val board = boardRepository.getReferenceById(boardId)
        if (board.seller.userId == userId) {
            boardRepository.delete(board)
            return true
        } else {
            throw BaseException(BaseExceptionCode.NOT_YOUR_BOARD)
        }
    }

    @Transactional
    fun getBoard(boardId: Long): BoardRes {
        val board = boardRepository.getReferenceById(boardId)
        board.viewCount++
        return BoardRes.toBoardRes(board)
    }

    @Transactional
    fun patchBoard(userId: String, boardId: Long, boardReq: BoardReq): Boolean {
        val board = boardRepository.getReferenceById(boardId)
        if (board.seller.userId == userId) {
            with(board) {
                category = boardReq.category
                title = boardReq.title
                description = boardReq.description
                price = boardReq.price
            }
        } else {
            throw BaseException(BaseExceptionCode.NOT_YOUR_BOARD)
        }
        return true
    }
}