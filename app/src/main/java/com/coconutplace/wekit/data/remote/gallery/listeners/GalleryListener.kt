package com.coconutplace.wekit.data.remote.gallery.listeners

import com.coconutplace.wekit.data.entities.PhotoPack

interface GalleryListener {
    fun addPhotoPack(photoPack: PhotoPack)
    fun makeSnackBar(str: String)
}