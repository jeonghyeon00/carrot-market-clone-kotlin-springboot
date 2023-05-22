package com.jeonghyeon00.kotlin.carrot.module.repository

import com.jeonghyeon00.kotlin.carrot.module.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long>
