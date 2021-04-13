package com.coconutplace.wekit.data.repository.channel

import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.entities.CreateChannelInfo
import com.coconutplace.wekit.data.remote.channel.ChannelResponse
import com.coconutplace.wekit.data.remote.channel.ChannelService
import com.coconutplace.wekit.data.repository.BaseRepository
import kotlinx.coroutines.flow.channelFlow

class ChannelRepository(private val channelService:ChannelService) : BaseRepository() {

    suspend fun createChannel(createChannelInfo: CreateChannelInfo) : ChannelResponse {
        return apiRequest { channelService.createChannel(createChannelInfo) }
    }

    suspend fun getRecentChannelList(page: Int): ChannelResponse{
        return apiRequest { channelService.getRecentChannelList(page) }
    }

    suspend fun getAllChannelList(page:Int): ChannelResponse{
        return apiRequest { channelService.getAllChannelList(page) }
    }

    suspend fun getFilteredChannelList(
        authCount: Int,
        roomTerm: Int,
        memberCount: Int,
        isStart: Int,
        page: Int
    ): ChannelResponse {
        return apiRequest {
            channelService.getFilteredChannelList(authCount, roomTerm, memberCount, isStart, page)
        }
    }

    suspend fun getSearchChannelList(
        content: String,
        page: Int
    ): ChannelResponse{
        return apiRequest { channelService.getSearchChannelList(content,page) }
    }

    suspend fun getMyChannel(): ChannelResponse{
        return apiRequest { channelService.getMyChannel() }
    }

    suspend fun enterChannel(chatRoom: ChatRoom) : ChannelResponse{
        return apiRequest { channelService.enterChannel(chatRoom) }
    }

    suspend fun leaveChannel(chatEx: ChatExtentions): ChannelResponse{
        return apiRequest { channelService.leaveChannel(chatEx) }
    }
}