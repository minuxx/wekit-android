package com.coconutplace.wekit.utils

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.entities.Photo
import com.coconutplace.wekit.ui.choice_photo.ChoicePhotoAdapter
import com.coconutplace.wekit.ui.diary.DiaryAdapter
import com.coconutplace.wekit.ui.notice.NoticeAdapter
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_ANGRY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_HAPPY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_SAD
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_SPEECHLESS
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_BLUNCH
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_BREAKFAST
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_DINNER
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_LINNER
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_LUNCH

object BindingUtils {
    //diary
    @BindingAdapter("setDiaries")
    @JvmStatic
    fun setDiaries(recyclerView: RecyclerView, items: ObservableArrayList<Diary>) {
        (recyclerView.adapter as DiaryAdapter).items = items
        recyclerView.adapter?.notifyDataSetChanged()
    }

    @BindingAdapter("urlImage")
    @JvmStatic
    fun ImageView.setUrlImage(url: String) {
        url.let {
            Glide.with(this)
                .load(url)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(this)
        }
    }

    @BindingAdapter("emojiResImage")
    @JvmStatic
    fun ImageView.setEmojiResImage(satisfaction: Int) {
        val resource = when (satisfaction) {
            SATISFACTION_HAPPY -> R.drawable.icn_emoji_happy_selected
            SATISFACTION_SPEECHLESS -> R.drawable.icn_emoji_speechless_selected
            SATISFACTION_SAD -> R.drawable.icn_emoji_sad_selected
            SATISFACTION_ANGRY -> R.drawable.icn_emoji_angry_selected
            else -> null
        }

        Glide.with(this).load(resource).into(this)
    }

    @BindingAdapter("convertTime")
    @JvmStatic
    fun TextView.convertTime(time: String) {
        val convertedMinute = if (time.substring(14, 16).toInt() < 10) {
            time.substring(15, 16)
        } else {
            time.substring(14, 16)
        }

        val convertedHour = if (time.substring(11, 13).toInt() < 10) {
            time.substring(12, 13)
        } else {
            time.substring(11, 13)
        }

        val convertedTime = if (convertedHour.toInt() < 12) {
                            "오전 ${convertedHour}시 ${convertedMinute}분"
                        } else if (convertedHour.toInt() == 12) {
                            "오후 ${convertedHour}시 ${convertedMinute}분"
                        } else {
                            "오후 ${convertedHour.toInt() - 12}시 ${convertedMinute}분"
                        }

        text = convertedTime
    }

    @BindingAdapter("convertTimezone")
    @JvmStatic
    fun TextView.convertTimezone(timezone: Int) {
        val timezoneString = when(timezone){
            TIMEZONE_BREAKFAST -> resources.getString(R.string.write_diary_breakfast)
            TIMEZONE_BLUNCH -> resources.getString(R.string.write_diary_blunch)
            TIMEZONE_LUNCH -> resources.getString(R.string.write_diary_lunch)
            TIMEZONE_LINNER -> resources.getString(R.string.write_diary_linner)
            TIMEZONE_DINNER -> resources.getString(R.string.write_diary_dinner)
            else -> ""
        }

        text = timezoneString
    }

    //notice
//    @BindingAdapter("setNotices")
//    @JvmStatic
//    fun setNotices(recyclerView: RecyclerView, items: ObservableArrayList<Notice>) {
//        (recyclerView.adapter as NoticeAdapter).items = items
//        recyclerView.adapter?.notifyDataSetChanged()
//    }

    @BindingAdapter("title")
    @JvmStatic
    fun TextView.noticeTitle(title: String) {
        text = title
    }

    @BindingAdapter("content")
    @JvmStatic
    fun TextView.noticeContent(content: String) {
        text = content
    }

    @BindingAdapter("date")
    @JvmStatic
    fun TextView.noticeDate(date: String) {
        text = date
    }
    
    //choice photo
    @BindingAdapter("setPhotos")
    @JvmStatic
    fun setPhotos(recyclerView: RecyclerView, items: ObservableArrayList<Photo>) {
        (recyclerView.adapter as ChoicePhotoAdapter).items = items
        recyclerView.adapter?.notifyDataSetChanged()
    }

    @BindingAdapter("uriImage")
    @JvmStatic
    fun ImageView.setUriImage(uri: String?) {
        uri?.let {
            Glide.with(this)
                .load(Uri.parse(uri))
                .into(this)
        }
    }
}
