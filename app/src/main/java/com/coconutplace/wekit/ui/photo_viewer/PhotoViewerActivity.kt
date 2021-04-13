package com.coconutplace.wekit.ui.photo_viewer

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.coconutplace.wekit.R
import com.coconutplace.wekit.utils.ChatMessageUtil
import com.coconutplace.wekit.utils.GlideUrlWithCacheKey
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.snackbar
import kotlinx.android.synthetic.main.activity_photo_viewer.*
import java.io.*
import java.util.*


class PhotoViewerActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo_viewer)

        val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")

        //Log.e(CHECK_TAG,"나의 android api : ${Build.VERSION.SDK_INT}")
        Log.e(CHECK_TAG,"PhotoViewer url : $url")

        photo_viewer_progress_bar.visibility = View.VISIBLE
        photo_viewer_back_btn.setOnClickListener { finish() }

        if(Build.VERSION.SDK_INT<29){
            grantExternalStoragePermission()
        }

        if(type != null && type.toLowerCase(Locale.ROOT).contains("gif")){
            ChatMessageUtil.displayGifPhotoFromUrl(this,url,photo_viewer_main_img,object: RequestListener<GifDrawable>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                    photo_viewer_progress_bar.visibility = View.GONE
                    photo_viewer_root_layout.snackbar("이미지를 로딩하는데에 실패하였습니다")
                    return false
                }

                override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    photo_viewer_progress_bar.visibility = View.GONE
                    photo_viewer_save_btn.visibility = View.VISIBLE
                    photo_viewer_save_btn.setOnClickListener {
                        photo_viewer_root_layout.snackbar("Gif 형식의 이미지 저장은 준비 중입니다")
                    }
                    return false
                }
            })

        }
        else{
            val cacheKey = url!!.split("?auth")

            Glide.with(this)
                .asBitmap()
                .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        photo_viewer_main_img.setImageBitmap(resource)
                        photo_viewer_progress_bar.visibility = View.GONE
                        photo_viewer_save_btn.visibility = View.VISIBLE
                        photo_viewer_save_btn.setOnClickListener {
                            if(Build.VERSION.SDK_INT>28){ //android Q 이상
                                saveBitmapAboveQ(resource,"image/jpeg","WEKIT_"+ChatMessageUtil.formatDateTime(System.currentTimeMillis()))
                            }
                            else{ //android 9이하 - MediaStore 사용불가
                                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    saveBitmapUnderPie(resource,"image/jpeg","WEKIT_"+ChatMessageUtil.formatDateTime(System.currentTimeMillis()))
                                }
                                else{
                                    grantExternalStoragePermission()
                                }
                            }
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) { }
                })
        }
    }

    private fun saveBitmapAboveQ(bitmap: Bitmap, mimeType: String, displayName: String): Uri? {
        val relativeLocation: String = Environment.DIRECTORY_DCIM + File.separator + "WEKIT"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        val resolver: ContentResolver = contentResolver
        var stream: OutputStream? = null
        var uri: Uri? = null
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            //val conte = MediaStore.Images.Media.INTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }
            stream = resolver.openOutputStream(uri)
            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                throw IOException("Failed to save bitmap.")
            }
            photo_viewer_root_layout.snackbar("사진 저장에 성공하였습니다")
        } catch (e: IOException) {
            photo_viewer_root_layout.snackbar("사진 저장에 실패하였습니다")
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            stream?.close()
        }
        return uri
    }

    private fun saveBitmapUnderPie(bitmap: Bitmap, mimeType: String, displayName: String){

        var res:String? = null

        res = MediaStore.Images.Media.insertImage(contentResolver, bitmap, displayName,"WEKIT_PHOTO")
        Log.e(CHECK_TAG,res)
        if(res==null){
            photo_viewer_root_layout.snackbar("사진 저장에 실패하였습니다")
        }
        else{
            photo_viewer_root_layout.snackbar("사진 저장에 성공하였습니다")
        }

//        val relativeLocation: String = Environment.DIRECTORY_DCIM + File.separator + "WEKIT"
//        val values= ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
//            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
//            put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation)
//            //put(MediaStore.Images.Media.DATA, file)
//        }
//        val resolver: ContentResolver = contentResolver
//        var stream: OutputStream? = null
//        var uri: Uri? = null
//        try {
//            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//
//            if (uri == null) {
//                throw IOException("Failed to create new MediaStore record.")
//            }
//            stream = resolver.openOutputStream(uri)
//            if (stream == null) {
//                throw IOException("Failed to get output stream.")
//            }
//            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
//                throw IOException("Failed to save bitmap.")
//            }
//            photo_viewer_root_layout.snackbar("사진 저장에 성공하였습니다")
//        } catch (e: IOException) {
//            photo_viewer_root_layout.snackbar("사진 저장에 실패하였습니다")
//            if (uri != null) {
//                // Don't leave an orphan entry in the MediaStore
//                resolver.delete(uri, null, null)
//            }
//            throw e
//        } finally {
//            stream?.close()
//        }
    }


    private fun grantExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= 28) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e(CHECK_TAG, "Permission is granted")
                true
            } else {
                Log.e(CHECK_TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            //Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show()
            Log.e(CHECK_TAG, "External Storage Permission is Grant ")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(CHECK_TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
                //resume tasks needing this permission
            }
        }
    }

}