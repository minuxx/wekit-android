package com.coconutplace.wekit.data.remote.gallery

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GalleryService {
    @GET("/chat/photo")
    suspend fun getGallery(
        @Query("idx") idx:Int,
        @Query("roomIdx")roomIdx:Int,
        @Query("page")page:Int
    ) : Response<GalleryResponse>

}