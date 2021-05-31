package com.coconutplace.wekit.ui.write_diary

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.entities.Photo
import com.coconutplace.wekit.data.remote.diary.listeners.WriteDiaryListener
import com.coconutplace.wekit.databinding.ActivityWriteDiaryBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.choice_photo.ChoicePhotoActivity
import com.coconutplace.wekit.utils.*
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_READ_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_WRITE_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQUEST_PHOTOS
import com.coconutplace.wekit.utils.GlobalConstant.Companion.RES_CODE_AUTH_SUCCESS
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_ANGRY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_HAPPY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_SAD
import com.coconutplace.wekit.utils.GlobalConstant.Companion.SATISFACTION_SPEECHLESS
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_BLUNCH
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_BREAKFAST
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_DINNER
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_LINNER
import com.coconutplace.wekit.utils.GlobalConstant.Companion.TIMEZONE_LUNCH
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class WriteDiaryActivity : BaseActivity(), WriteDiaryListener {
    private lateinit var binding: ActivityWriteDiaryBinding
    private val viewModel: WriteDiaryViewModel by viewModel()
    private lateinit var pagerAdapter: PhotoPagerAdapter
    private var selectedDate: String? = null
    private var mFlag: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_write_diary)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.writeDiaryListener = this

        mFlag = intent.getIntExtra("flag", 0)

        if (mFlag == 0) {
            finish()
        }

        initPhotoViewPager()
        setOnClickListenerAll()
        observeSatisfaction()
        observeTimezone()

        selectedDate = intent.getStringExtra("date")
        viewModel.setDate(selectedDate ?: "")
        
        setMode()
    }

    private fun setMode() {
        when (mFlag) {
            FLAG_CERTIFY_DIARY -> {
                binding.writeDiaryEventBtn.text = "보내기"

                val roomIdx: Int = intent.getIntExtra("roomIdx", 0)
                if (roomIdx != 0)
                    viewModel.setRoomIdx(roomIdx)
            }
            FLAG_WRITE_DIARY -> {
            }
            FLAG_READ_DIARY -> {
                binding.writeDiaryTitleTv.text = "식단일기"
                binding.writeDiaryEventBtn.visibility = GONE
                binding.writeDiaryPickPhotoBtn.visibility = GONE
//                binding.writeDiaryEditTv.visibility = VISIBLE
                binding.writeDiaryDefaultIv.visibility = GONE

                binding.writeDiarySatisfactionHappyIv.isClickable = false
                binding.writeDiarySatisfactionSpeechlessIv.isClickable = false
                binding.writeDiarySatisfactionSadIv.isClickable = false
                binding.writeDiarySatisfactionAngryIv.isClickable = false

                binding.writeDiaryBreakfastTv.isClickable = false
                binding.writeDiaryBlunchTv.isClickable = false
                binding.writeDiaryLunchTv.isClickable = false
                binding.writeDiaryLinnerTv.isClickable = false
                binding.writeDiaryDinnerTv.isClickable = false

                binding.writeDiaryMemoEt.isFocusableInTouchMode = false

                val diaryInx = intent.getIntExtra("diaryIdx", -1)

                if (diaryInx == -1) {
                    finish()
                }

                viewModel.getDiary(diaryInx)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.writeDiaryPickPhotoBtn.isClickable = true
        binding.writeDiaryDefaultIv.isClickable = true
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when (v) {
            binding.writeDiaryBackBtn -> finish()
            binding.writeDiaryPickPhotoBtn, binding.writeDiaryDefaultIv -> startChoicePhotoActivity()
            binding.writeDiaryRootLayout -> binding.writeDiaryRootLayout.hideKeyboard()

            binding.writeDiarySatisfactionHappyIv -> viewModel.satisfaction.postValue(
                SATISFACTION_HAPPY
            )
            binding.writeDiarySatisfactionSpeechlessIv -> viewModel.satisfaction.postValue(
                SATISFACTION_SPEECHLESS
            )
            binding.writeDiarySatisfactionSadIv -> viewModel.satisfaction.postValue(SATISFACTION_SAD)
            binding.writeDiarySatisfactionAngryIv -> viewModel.satisfaction.postValue(
                SATISFACTION_ANGRY
            )

            binding.writeDiaryBreakfastTv -> viewModel.timezone.postValue(TIMEZONE_BREAKFAST)
            binding.writeDiaryBlunchTv -> viewModel.timezone.postValue(TIMEZONE_BLUNCH)
            binding.writeDiaryLunchTv -> viewModel.timezone.postValue(TIMEZONE_LUNCH)
            binding.writeDiaryLinnerTv -> viewModel.timezone.postValue(TIMEZONE_LINNER)
            binding.writeDiaryDinnerTv -> viewModel.timezone.postValue(TIMEZONE_DINNER)


            binding.writeDiaryEventBtn -> {
                binding.writeDiaryEventBtn.isClickable = false
                viewModel.uploadToFirebase()
            }

//            binding.writeDiaryEditTv -> binding.writeDiaryRootLayout.snackbar(getString(R.string.guide_update))
        }
    }

    private fun setOnClickListenerAll() {
        binding.writeDiaryRootLayout.setOnClickListener(this)
        binding.writeDiaryBackBtn.setOnClickListener(this)
        binding.writeDiaryPickPhotoBtn.setOnClickListener(this)
//        binding.writeDiaryEditTv.setOnClickListener(this)
        binding.writeDiaryDefaultIv.setOnClickListener(this)

        binding.writeDiarySatisfactionHappyIv.setOnClickListener(this)
        binding.writeDiarySatisfactionSpeechlessIv.setOnClickListener(this)
        binding.writeDiarySatisfactionSadIv.setOnClickListener(this)
        binding.writeDiarySatisfactionAngryIv.setOnClickListener(this)

        binding.writeDiaryBreakfastTv.setOnClickListener(this)
        binding.writeDiaryBlunchTv.setOnClickListener(this)
        binding.writeDiaryLunchTv.setOnClickListener(this)
        binding.writeDiaryLinnerTv.setOnClickListener(this)
        binding.writeDiaryDinnerTv.setOnClickListener(this)

        binding.writeDiaryEventBtn.setOnClickListener(this)
        binding.writeDiaryEventBtn.isClickable = false

        binding.writeDiaryEventBtn.setOnClickListener(this)
    }

    private fun setIsClickable(isClickable: Boolean) {
        binding.writeDiaryBackBtn.isClickable = isClickable
        binding.writeDiaryPickPhotoBtn.isClickable = isClickable
//        binding.writeDiaryEditTv.isClickable = isClickable
        binding.writeDiaryDefaultIv.isClickable = isClickable

        binding.writeDiarySatisfactionHappyIv.isClickable = isClickable
        binding.writeDiarySatisfactionSpeechlessIv.isClickable = isClickable
        binding.writeDiarySatisfactionSadIv.isClickable = isClickable
        binding.writeDiarySatisfactionAngryIv.isClickable = isClickable

        binding.writeDiaryBreakfastTv.isClickable = isClickable
        binding.writeDiaryBlunchTv.isClickable = isClickable
        binding.writeDiaryLunchTv.isClickable = isClickable
        binding.writeDiaryLinnerTv.isClickable = isClickable
        binding.writeDiaryDinnerTv.isClickable = isClickable

        binding.writeDiaryEventBtn.isClickable = isClickable
    }

    private fun initPhotoViewPager() {
        pagerAdapter = PhotoPagerAdapter()
        binding.writeDiaryPager.adapter = pagerAdapter
    }

    private fun addView(newPage: View) {
        val pageIndex = pagerAdapter.addView(newPage)
    }

    private fun removeView(defunctPage: View?) {
        var pageIndex = pagerAdapter.removeView(binding.writeDiaryPager, defunctPage)
        if (pageIndex == pagerAdapter.count) pageIndex--
        binding.writeDiaryPager.currentItem = pageIndex
    }

    private fun removeViewAll() {
        if(pagerAdapter.count != 0) {
            for (i in (pagerAdapter.count - 1) downTo 0) {
                removeView(pagerAdapter.getView(i))
            }
        }
    }

    private fun startChoicePhotoActivity() {
        binding.writeDiaryPickPhotoBtn.isClickable = false
        binding.writeDiaryDefaultIv.isClickable = false

        val intent = Intent(this, ChoicePhotoActivity::class.java)
        intent.putExtra("flag", mFlag)

        if(viewModel.getPhotoCount() > 1){
            val gson = Gson()

            val arrayPhotoType = object : TypeToken<ArrayList<Photo>>() {}.type
            val itemsJson : String = gson.toJson(viewModel.getPhotos(), arrayPhotoType)

            intent.putExtra("photo-items", itemsJson)
        }

        startActivityForResult(intent, REQUEST_PHOTOS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PHOTOS) {
                val gson = Gson()
                val arrayPhotoType = object : TypeToken<ArrayList<Photo>>() {}.type
                val photos: ArrayList<Photo> = gson.fromJson(
                    data!!.getStringExtra("photo-items"),
                    arrayPhotoType
                )

                viewModel.addPhotos(photos)

                removeViewAll()

                for (i in 1 until viewModel.getPhotoCount()) {
                    val view: View = layoutInflater.inflate(R.layout.fragment_diary_photo, null)
                    val imageView = view.findViewById<ImageView>(R.id.write_diary_photo_iv)
                    imageView.adjustViewBounds = true
//                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

//                    Glide.with(this)
//                        .load(viewModel.getPhotoUri(i))
//                        .into(imageView)

//                    var bitmap: Bitmap? = null
//
                    drawTextToBitmap(viewModel.getPhotoUri(i)!!)?.let{
                        Glide.with(this)
                            .asBitmap()
                            .load(it)
                            .into(imageView)

                        viewModel.saveBitmap(i, it)
                    }

                    addView(view)
                }

                binding.writeDiaryDefaultIv.visibility = GONE

                if(viewModel.getPhotoCount() == 1){
                    binding.writeDiaryDefaultIv.visibility = VISIBLE
                }

                pagerAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun observeSatisfaction() {
        viewModel.satisfaction.observe(this, Observer { satisfaction ->
            binding.writeDiarySatisfactionHappyIv.isSelected = false
            binding.writeDiarySatisfactionSpeechlessIv.isSelected = false
            binding.writeDiarySatisfactionSadIv.isSelected = false
            binding.writeDiarySatisfactionAngryIv.isSelected = false

            when (satisfaction) {
                SATISFACTION_HAPPY -> binding.writeDiarySatisfactionHappyIv.isSelected = true
                SATISFACTION_SPEECHLESS -> binding.writeDiarySatisfactionSpeechlessIv.isSelected = true
                SATISFACTION_SAD -> binding.writeDiarySatisfactionSadIv.isSelected = true
                SATISFACTION_ANGRY -> binding.writeDiarySatisfactionAngryIv.isSelected = true
            }
        })
    }

    private fun observeTimezone() {
        viewModel.timezone.observe(this, Observer { timezone ->
            convertTimezoneView(binding.writeDiaryBreakfastTv, false)
            convertTimezoneView(binding.writeDiaryBlunchTv, false)
            convertTimezoneView(binding.writeDiaryLunchTv, false)
            convertTimezoneView(binding.writeDiaryLinnerTv, false)
            convertTimezoneView(binding.writeDiaryDinnerTv, false)

            when (timezone) {
                TIMEZONE_BREAKFAST -> convertTimezoneView(binding.writeDiaryBreakfastTv, true)
                TIMEZONE_BLUNCH -> convertTimezoneView(binding.writeDiaryBlunchTv, true)
                TIMEZONE_LUNCH -> convertTimezoneView(binding.writeDiaryLunchTv, true)
                TIMEZONE_LINNER -> convertTimezoneView(binding.writeDiaryLinnerTv, true)
                TIMEZONE_DINNER -> convertTimezoneView(binding.writeDiaryDinnerTv, true)
            }
        })
    }

    private fun convertTimezoneView(v: TextView, isActivated: Boolean) {
        v.isSelected = isActivated
        if (isActivated) {
            v.setTextColor(getColor(R.color.write_diary_timezone_selected))
        } else {
            v.setTextColor(getColor(R.color.write_diary_timezone_unselected))
        }
    }

    //draw text on photo
    private fun drawTextToBitmap(uri: String): Bitmap? {
        val fileIs: InputStream? = contentResolver.openInputStream(Uri.parse(uri))
        val bitmap: Bitmap? = BitmapFactory.decodeStream(fileIs).copy(Bitmap.Config.ARGB_8888, true)
        var drawBitmap: Bitmap? = null

        bitmap?.let {
            try {
                val scale: Float = resources.displayMetrics.density
                var config: Bitmap.Config? = bitmap!!.config

                if (config == null) {
                    config = Bitmap.Config.ARGB_8888
                }

                val newBitmap = Bitmap.createBitmap(it.width, it.height, config)
                val newCanvas = Canvas(newBitmap)

                newCanvas.drawBitmap(it, 0f, 0f, null)

                val currentDateTime:String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.now().toString()
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Calendar.getInstance().time)
                }

//                Log.d(DEBUG_TAG, "currDateTime: $currentDateTime")

                val captionString = convertDateTimeFormat(currentDateTime)
                val rectText = Rect()

                val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
                paintText.color = ContextCompat.getColor(this, R.color.white)
                paintText.textSize =  48f
                paintText.typeface = Typeface.createFromAsset(assets, "notosanskr_bold.otf")
                paintText.getTextBounds(captionString, 0, captionString.length, rectText)

                val logoMark = BitmapFactory.decodeResource(resources, R.drawable.icn_wekit_mark)

//                Log.d(DEBUG_TAG, "width: ${newBitmap.width}, height: ${newBitmap.height}")

                val y = ((newBitmap.height + rectText.height()) / 3) * scale
                newCanvas.drawText(captionString, 50f, y, paintText)
                newCanvas.drawBitmap(logoMark, newBitmap.width - logoMark.width - 50f, y - logoMark.height, paintText)

                drawBitmap = newBitmap
            } catch (e: FileNotFoundException) {
                drawBitmap = null
            }
        }

        return drawBitmap
    }

    //2021-01-23T21:32:44.333
    private fun convertDateTimeFormat(date: String): String {
        val month = if (date.substring(5, 7).toInt() < 10) {
            date.substring(6, 7)
        } else {
            date.substring(5, 7)
        }

        val day = if (date.substring(8, 10).toInt() < 10) {
            date.substring(9, 10)
        } else {
            date.substring(8, 10)
        }

        val meridiem = if (date.substring(11, 13).toInt() < 12){
            "AM "
        } else {
            "PM "
        }

        val hour = meridiem + if (date.substring(11, 13).toInt() < 10) {
            date.substring(12, 13)
        } else if(date.substring(11, 13).toInt() <= 12){
            date.substring(11, 13).toInt()
        } else {
            date.substring(11, 13).toInt() - 12
        }

        val minute = if (date.substring(14, 16).toInt() < 10) {
            date.substring(15, 16)
        } else {
            date.substring(14, 16)
        }

        return "${hour}시 ${minute}분 ${month}월 ${day}일"
    }

    override fun onUploadToFirebaseStarted() {
        binding.writeDiaryLoading.show()

        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onUploadToFirebaseSuccess() {
        binding.writeDiaryLoading.hide()

        if (viewModel.getUriCount() == viewModel.getTriedUploadCount()) {
            viewModel.postDiary()
        }
    }

    override fun onUploadToFirebaseFailure() {
        binding.writeDiaryLoading.hide()

        binding.writeDiaryEventBtn.isClickable = true
        if (viewModel.getUriCount() == viewModel.getTriedUploadCount()) {
            viewModel.postDiary()
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onPostDiaryStarted() {
        binding.writeDiaryLoading.show()

        setIsClickable(false)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onPostDiarySuccess(message: String) {
        binding.writeDiaryLoading.hide()

        when (mFlag) {
            FLAG_CERTIFY_DIARY -> {
                val intent = Intent()
                intent.putExtra("imgList", viewModel.getImgUrlsFromFirebase())
                setResult(RES_CODE_AUTH_SUCCESS, intent)
            }
        }

        finish()
    }

    override fun onPostDiarySuccess(
        message: String,
        badgeTitle: String,
        badgeUrl: String,
        badgeExplain: String,
        backgroundColor: String
    ) {
        binding.writeDiaryLoading.hide()

        when(mFlag){
            FLAG_CERTIFY_DIARY -> {
                val intent = Intent()
                intent.putExtra("imgList", viewModel.getImgUrlsFromFirebase())
                intent.putExtra("badgeTitle", badgeTitle)
                intent.putExtra("badgeUrl", badgeUrl)
                intent.putExtra("badgeExplain", badgeExplain)
                intent.putExtra("backgroundColor", backgroundColor)
                setResult(RES_CODE_AUTH_SUCCESS, intent)
            }
        }

        finish()
    }

    override fun onPostDiaryFailure(code: Int, message: String) {
        binding.writeDiaryLoading.hide()
        binding.writeDiaryEventBtn.isClickable = true
        when(code){
            305, 306, 307,
            308, 310, 314 -> showDialog(message)
            else -> showDialog(getString(R.string.network_error))
        }

        setIsClickable(true)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        viewModel.clearTiredUpload()
    }


    override fun onGetDiaryStarted() {
        binding.writeDiaryLoading.show()
    }

    override fun onGetDiarySuccess(diary: Diary) {
        binding.writeDiaryLoading.hide()

        binding.writeDiaryDefaultIv.visibility = GONE

        for (url in diary.imageList) {
            val view: View = layoutInflater.inflate(R.layout.fragment_diary_photo, null)
            val imageView = view.findViewById<ImageView>(R.id.write_diary_photo_iv)

            Glide.with(this)
                .load(url)
                .into(imageView)

            addView(view)
        }

        pagerAdapter.notifyDataSetChanged()

        viewModel.satisfaction.postValue(diary.satisfaction)
        viewModel.timezone.postValue(diary.timezone)

        viewModel.memo.postValue(diary.memo)
        if(diary.memo == "" && mFlag == FLAG_READ_DIARY){
            binding.writeDiaryMemoEt.hint = ""
        }
    }

    override fun onGetDiaryFailure(code: Int, message: String) {
        binding.writeDiaryLoading.hide()
        when(code){
            303, 304, 305 -> finish()
            else -> showDialog(getString(R.string.dialog_title_server_check))
        }
    }
}

//fun getCurrentPage(): View? {
//    return pagerAdapter.getView(binding.writeDiaryPager.currentItem)
//}
//
//private fun setCurrentPage(pageToShow: View?) {
//    binding.writeDiaryPager.setCurrentItem(pagerAdapter.getItemPosition(pageToShow!!), true)
//}

//    private lateinit var mDestinationUri: Uri

//    private fun startCropActivity(uri: Uri){
//        var uCrop = UCrop.of(uri, mDestinationUri)
//        var options: UCrop.Options = UCrop.Options()
//
//        options.setActiveControlsWidgetColor(getColor(R.color.primary))
//        options.setFreeStyleCropEnabled(true)
//        uCrop.withOptions(options)
//
//        uCrop.start(this@WriteDiaryActivity)
//    }

//    private fun isMatchedDate(takenDate: String): Boolean {
//        return selectedDate == takenDate
//    }
//
//    private fun isWritable(): Boolean {
//        return pagerAdapter.count < 5
//    }
//
//    private fun exifOrientationToDegree(exifOrientation: Int): Int {
//        return when (exifOrientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> 90
//            ExifInterface.ORIENTATION_ROTATE_180 -> 180
//            ExifInterface.ORIENTATION_ROTATE_270 -> 270
//            else -> 0
//        }
//    }
//
//    private fun rotatePhoto(bitmap: Bitmap, degree: Int): Bitmap {
//        val matrix = Matrix()
//        matrix.postRotate(degree.toFloat())
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//    }

    //extract photo bitmap, date
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun extractPhoto(uri: Uri): Photo {
//        val photo = Photo(null, null)
//        lateinit var exif: ExifInterface
//
//        try {
//            val inputStream = applicationContext.contentResolver.openInputStream(uri)
//            val cursor = applicationContext.contentResolver.query(uri, null, null, null, null)
//
//            cursor?.use { c ->
//                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                val dateTakenIndex =
//                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
//
//                if (c.moveToFirst()) {
//                    val name = c.getString(nameIndex)
//                    val millis = c.getLong(dateTakenIndex)
//                    val date = LocalDateTime.ofInstant(
//                        Instant.ofEpochMilli(millis),
//                        ZoneId.systemDefault()
//                    )
//                    photo.date = date.toString()
//
//                    inputStream?.let { inputStream ->
//                        val file = File(applicationContext.cacheDir, name)
//
//                        val os = file.outputStream()
//
//                        os.use {
//                            inputStream.copyTo(it)
//                        }
//
//                        exif = ExifInterface(file.absolutePath)
//                        val exifOrientation = exif.getAttributeInt(
//                            ExifInterface.TAG_ORIENTATION,
//                            ExifInterface.ORIENTATION_NORMAL
//                        )
//                        val exifDegree = exifOrientationToDegree(exifOrientation)
//
//                        photo.bitmap =
//                            rotatePhoto(BitmapFactory.decodeFile(file.absolutePath), exifDegree)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            binding.writeDiaryRootLayout.snackbar("Error: " + e.message)
//        }
//
//        return photo
//    }

    //draw text on photo
//    private fun drawTextToBitmap(photo: Photo): Bitmap? {
//        return try {
//            val bitmap: Bitmap = photo.bitmap!!
//            val scale: Float = resources.displayMetrics.density
//            var config: Bitmap.Config? = bitmap.config
//
//            if (config == null) {
//                config = Bitmap.Config.ARGB_8888
//            }
//
//            val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
//            val newCanvas = Canvas(newBitmap)
//
//            newCanvas.drawBitmap(bitmap, 0f, 0f, null)
//
//            val captionString = convertDate(photo.date!!)
//
//            val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
//            paintText.color = ContextCompat.getColor(this, R.color.white)
//            paintText.textSize = 100f
//            paintText.typeface = Typeface.createFromAsset(assets, "notosanskr_bold.otf")
//
//            val rectText = Rect()
//            paintText.getTextBounds(captionString, 0, captionString.length, rectText)
//
//            val y = ((newBitmap.height + rectText.height()) / 3) * scale
//            newCanvas.drawText(
//                captionString,
//                100f, y.toFloat(), paintText
//            )
//            newBitmap
//        } catch (e: FileNotFoundException) {
//            binding.writeDiaryRootLayout.snackbar("Error: " + e.message)
//            null
//        }
//    }

    //2021-01-23T21:32:44.333
//    private fun convertDate(date: String): String {
//        val month = if (date.substring(5, 7).toInt() < 10) {
//            date.substring(6, 7)
//        } else {
//            date.substring(5, 7)
//        }
//
//        val day = if (date.substring(8, 10).toInt() < 10) {
//            date.substring(9, 10)
//        } else {
//            date.substring(8, 10)
//        }
//
//        val hour = if (date.substring(11, 13).toInt() < 12) {
//            "AM ${date.substring(12, 13)}"
//        } else {
//            "PM ${date.substring(11, 13).toInt() - 12}"
//        }
//
//        val minute = if (date.substring(14, 16).toInt() < 10) {
//            date.substring(15, 16)
//        } else {
//            date.substring(14, 16)
//        }
//
//        return "${hour}시 ${minute}분 ${month}월 ${day}일"
//    }
//


//    //startActivityForResult deprecated
//    private fun pickImageFromGallery() {
//        binding.writeDiaryPickPhotoBtn.isClickable = false
//        if (!isWritable()) {
//            binding.writeDiaryRootLayout.snackbar(getString(R.string.write_diary_max_photo_count))
//            binding.writeDiaryPickPhotoBtn.isClickable = true
//            return
//        }
//
//        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        gallery.type = "image/*"
////        startActivityForResult(gallery, IMAGE_PICK_CODE)
//
//        startActivityForResult(Intent.createChooser(gallery, "이미지선택"), UCrop.REQUEST_CROP);
//    }

//    private lateinit var destinationUri : Uri;
//2021-01-23T21:32:44.333
//    @RequiresApi(Build.VERSION_CODES.Q)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(resultCode == RESULT_OK) {
//            if (requestCode == IMAGE_PICK_CODE) {
//                if (data != null && data.data != null) {
//                    val view: View = layoutInflater.inflate(R.layout.fragment_diary_photo, null)
//                    val imageView = view.findViewById<ImageView>(R.id.write_diary_photo_iv)
//                    imageView.adjustViewBounds = true
//
//                    val photo = extractPhoto(data.data!!)
//
//                    UCrop.of(data.data!!, destinationUri)
//                        .withAspectRatio(16F, 9.0F)
//                        .start(this);
//
//                    if (isMatchedDate(photo.date!!.substring(0, 10))) {
//                        val bitmap = when (mFlag) {
//                            FLAG_CERTIFY_DIARY -> drawTextToBitmap(photo)!!
//                            else -> photo.bitmap!!
//                        }
//
//                        viewModel.addImgBitmap(bitmap)
//
//                        Glide.with(this)
//                            .load(bitmap)
//                            .centerCrop()
//                            .into(imageView)
//
//                        binding.writeDiaryDefaultIv.visibility = GONE
//                        addView(view)
//                        pagerAdapter.notifyDataSetChanged()
//                    } else {
//                        binding.writeDiaryRootLayout.snackbar(getString(R.string.write_diary_not_match_date))
//                    }
//                }
//            }else if(requestCode == UCrop.REQUEST_CROP) {
//                startCropActivity(data!!.data!!)
//            } else if(resultCode == UCrop.RESULT_ERROR){
//                 val cropError = UCrop.getError(data!!);
//            }
//        }
//    }