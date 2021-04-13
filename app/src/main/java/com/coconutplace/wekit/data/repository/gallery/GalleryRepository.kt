package com.coconutplace.wekit.data.repository.gallery

import com.coconutplace.wekit.data.remote.gallery.GalleryResponse
import com.coconutplace.wekit.data.remote.gallery.GalleryService
import com.coconutplace.wekit.data.repository.BaseRepository

class GalleryRepository(private val galleryService:GalleryService) : BaseRepository(){

    suspend fun getGallery(idx:Int,roomIdx:Int,page:Int) : GalleryResponse{
        return apiRequest{galleryService.getGallery(idx,roomIdx,page)}
    }
}