package com.coconutplace.wekit.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.coconutplace.wekit.R
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageUtil {
    companion object {
        fun formatTime(timeInMillis: Long): String? {
            val dateFormat = SimpleDateFormat("a hh:mm", Locale.KOREA)
            return dateFormat.format(timeInMillis)
        }

        fun formatDate(timeInMillis: Long): String {
            val dateFormat = SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREA)
            return dateFormat.format(timeInMillis)
        }

        fun formatDateTime(timeInMillis: Long): String{
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA)
            return dateFormat.format(timeInMillis)
        }

        fun displayImageFromUrl(
            context: Context?,
            url: String?,
            imageView: ImageView?,
            listener: RequestListener<Drawable>?
        ) {

            val cacheKey = url!!.split("?auth")
            val myOptions = RequestOptions()
                .dontAnimate()
            //.diskCacheStrategy(DiskCacheStrategy.NONE)
            if (listener != null) {
                Glide.with(context!!)
                    .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                    .thumbnail(0.4f)
                    .placeholder(R.drawable.img_chat_placeholder)
                    .apply(myOptions)
                    .listener(listener)
                    //.override(300)
                    .centerCrop()
                    .into(imageView!!)
            } else {
                Glide.with(context!!)
                    .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                    .thumbnail(0.4f)
                    .placeholder(R.drawable.img_chat_placeholder)
                    .apply(myOptions)
                    .listener(listener)
                    //.override(300)
                    .centerCrop()
                    .into(imageView!!)
            }
        }

        fun displayProfile(context: Context, url: String?, imageView: ImageView) {
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.character_big_sunglasses)
                .circleCrop()
                .override(80)
                .into(imageView)
        }

        fun displayGifImageFromUrl(
            context: Context?,
            url: String?,
            imageView: ImageView?,
            thumbnailUrl: String?
        ) {
            val cacheKey = url!!.split("?auth")

            if (thumbnailUrl != null) {
                Glide.with(context!!)
                    .asGif()
                    .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.character_big_smile)
                    .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                    .into(imageView!!)
            } else {
                Glide.with(context!!)
                    .asGif()
                    .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.character_big_smile)
                    .into(imageView!!)
            }
        }

        fun displayGifPhotoFromUrl(
            context: Context,
            url: String?,
            imageView: ImageView,
            listener: RequestListener<GifDrawable>?
        ) {
            val cacheKey = url!!.split("?auth")

            Glide.with(context)
                .asGif()
                .load(GlideUrlWithCacheKey(url, cacheKey[0]))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(listener)
                .into(imageView)
        }
    }
}


