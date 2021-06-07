package com.coconutplace.wekit.data.entities

import android.graphics.Bitmap
import com.coconutplace.wekit.utils.GlobalConstant.Companion.ITEM_TYPE_PHOTO

data class Photo(
    var bitmap: Bitmap?,
    var date: String?,
    var imgUrl: String?,
    var type: Int = ITEM_TYPE_PHOTO
)