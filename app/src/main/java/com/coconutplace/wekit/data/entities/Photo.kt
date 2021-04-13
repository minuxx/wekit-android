package com.coconutplace.wekit.data.entities

import android.graphics.Bitmap
import android.net.Uri
import com.coconutplace.wekit.utils.GlobalConstant.Companion.ITEM_TYPE_PHOTO

data class Photo(
    var bitmap: Bitmap?,
    var date: String?,
    var imgUrl: String?

){
    var type = ITEM_TYPE_PHOTO
}