package com.coconutplace.wekit.data.entities

import java.io.Serializable

data class ChannelFilter(
    val authCount:Int,
    val isTwoWeek:Boolean,
    val memberCount: Int,
    val isOngoing:Boolean
):Serializable