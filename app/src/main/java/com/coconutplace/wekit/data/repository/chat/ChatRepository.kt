package com.coconutplace.wekit.data.repository.chat

import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.chat.ChatResponse
import com.coconutplace.wekit.data.remote.chat.ChatService
import com.coconutplace.wekit.data.repository.BaseRepository

class ChatRepository (private val chatService: ChatService) : BaseRepository(){
    suspend fun leaveChannel(chatEx: ChatExtentions): ChatResponse {
        return apiRequest { chatService.leaveChannel(chatEx) }
    }
    suspend fun getRoomInfo(roomIdx: Int): ChatResponse{
        return apiRequest { chatService.getRoomInfo(roomIdx) }
    }
    suspend fun reportChannel(chatEx: ChatExtentions): ChatResponse{
        return apiRequest { chatService.reportChannel(chatEx) }
    }
    suspend fun expelMember(chatEx: ChatExtentions): ChatResponse{
        return apiRequest { chatService.expelMember(chatEx) }
    }
    suspend fun postDiaryWithChat(diary: Diary): ChatResponse{
        return apiRequest { chatService.postDiaryWithChat(diary) }
    }
    suspend fun startChallenge(chatEx: ChatExtentions): ChatResponse{
        return apiRequest { chatService.startChallenge(chatEx) }
    }
    suspend fun checkChallenge(chatEx: ChatExtentions) : ChatResponse{
        return apiRequest { chatService.checkChallenge(chatEx) }
    }
}