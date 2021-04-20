package com.coconutplace.wekit.ui.choice_photo

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Photo

class ChoiceViewModel() : ViewModel() {
    val photos = ObservableArrayList<Photo>()

    fun addPhotos(photos: ArrayList<Photo>){
        this.photos.clear()
        this.photos.addAll(photos)
    }

    fun addPhoto(photo: Photo){
        this.photos.add(photo)
    }

    fun getPhotoCount() : Int{
        return this.photos.size
    }

    fun getPhotos(): ArrayList<Photo>{
        return this.photos
    }
}