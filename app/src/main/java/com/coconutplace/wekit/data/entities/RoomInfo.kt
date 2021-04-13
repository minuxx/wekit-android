package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class RoomInfo ( //방 내부 멤버 정보

    @SerializedName(value = "totalAuthenticCount")
    var totalAuthenticCount: Int?,
    @SerializedName(value = "totalMember")
    var totalMember: Int?,
    @SerializedName(value = "day")
    var day:Int?,
    @SerializedName(value = "totalDay")
    var totalDay:Int?,
    @SerializedName(value = "certificationCount")
    var certificationCount: Int?,
    @SerializedName(value = "startDate")
    var startDate:String?,
    @SerializedName(value = "endDate")
    var endDate:String?,
    @SerializedName(value = "isStart")
    var isStart:String?,
    @SerializedName(value = "isNotice")
    var isNotice:String?,
    @SerializedName(value = "userInfo")
    var userInfo:ArrayList<UserInfo>?,
    @SerializedName(value = "badgeName")
    var badgeName:String?,
    @SerializedName(value = "badgeImageUrl")
    var badgeImageUrl:String?,
    @SerializedName(value = "badgeDescription")
    var badgeDescription:String?,
    @SerializedName(value = "backgroundColor")
    var backgroundColor:String?

)

