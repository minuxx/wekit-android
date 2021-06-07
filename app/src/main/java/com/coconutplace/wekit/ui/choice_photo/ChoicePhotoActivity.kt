package com.coconutplace.wekit.ui.choice_photo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.coconutplace.wekit.BuildConfig
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Photo
import com.coconutplace.wekit.databinding.ActivityChoicePhotoBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DEBUG_TAG
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_WRITE_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.ITEM_TYPE_ADD_PHOTO
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQUEST_SELECT_PICTURE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQUEST_TAKE_PICTURE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.yalantis.ucrop.UCrop
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChoicePhotoActivity : BaseActivity() {
    private lateinit var binding: ActivityChoicePhotoBinding
    private val viewModel: ChoiceViewModel by viewModel()
    private lateinit var adapter: ChoicePhotoAdapter
    private var mFlag: Int = 0
    var mImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choice_photo)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val date = CalendarDay.today()
        val dateFormat = date.year.toString() + "." + if (date.month < 10) {"0" + date.month} else { date.month.toString() } + "." + if (date.day < 10) {"0" + date.day} else { date.day.toString() }

        binding.choicePhotoDateTv.text = dateFormat

        binding.choicePhotoBackBtn.setOnClickListener(this)
        binding.choicePhotoSaveBtn.setOnClickListener(this)

        mFlag = intent.getIntExtra("flag", 0)

        if (mFlag == 0) {
            finish()
        }

        initRecyclerView()
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when (v) {
            binding.choicePhotoBackBtn -> finish()
            binding.choicePhotoSaveBtn -> savePhotos()
        }
    }

    private fun initRecyclerView() {
        val gridLayoutManager = GridLayoutManager(applicationContext, 3)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL

        adapter = ChoicePhotoAdapter(applicationContext, itemClick = {
            if (it == 0) {
                if (adapter.itemCount > 5) {
                    showDialog(getString(R.string.choice_photo_count_limit))
                } else {
                    if (mFlag == FLAG_CERTIFY_DIARY) {
                        launchCameraActivity() // 인증 다이어리(사진)을 보낼 때는 카메라로 찍은 사진만 가능
                    } else if (mFlag == FLAG_WRITE_DIARY) {
                        pickImageFromGallery() // 일반 다이어리(사진)을 생성할 때는 갤러리, 카메라 모두 가능
                    }
                }
            } else {
                adapter.removeItem(it)
            }
        })

        binding.choicePhotoRecyclerview.layoutManager = gridLayoutManager
        binding.choicePhotoRecyclerview.addItemDecoration(GridSpacingItemDecoration(3, 1, false))
        binding.choicePhotoRecyclerview.adapter = adapter

        adapter.items = viewModel.photos

        intent!!.getStringExtra("photo-items")?.let{
            val gson = Gson()
            val arrayPhotoType = object : TypeToken<ArrayList<Photo>>() {}.type
            val photos: ArrayList<Photo> = gson.fromJson(it, arrayPhotoType)

//            viewModel.addPhotos(photos)
            adapter.addItems(photos)
        }

        if(viewModel.getPhotoCount() == 0){
            val addPhotoItem = Photo(null, null, null)
            addPhotoItem.type = ITEM_TYPE_ADD_PHOTO

//            viewModel.addPhoto(addPhotoItem)
            adapter.addItem(addPhotoItem)
        }
    }

    private fun pickImageFromGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        gallery.type = "image/*"
        //startActivityForResult(gallery, IMAGE_PICK_CODE)

        startActivityForResult(
            Intent.createChooser(
                gallery,
                getString(R.string.choice_photo_label_select_image)
            ), REQUEST_SELECT_PICTURE
        )
    }

    private fun launchCameraActivity() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(takePictureIntent.resolveActivity(packageManager) != null) {
            createImageFile().let {
                val photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", it)

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
                mImageFile = it
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val imageFileName = "diary_${timeStamp}.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File(storageDir, imageFileName)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_SELECT_PICTURE -> {
                    startCropActivity(data!!.data!!)
                }
                REQUEST_TAKE_PICTURE -> {
//                    Log.d(DEBUG_TAG, "imageFile: "+ m_imageFile.toString())

                    mImageFile?.let {
                        startCropActivity(Uri.fromFile(it))
                    }

                }

                UCrop.REQUEST_CROP -> {
                    UCrop.getOutput(data!!)?.let {
//                        Log.d("ChoicePhotoDebug://", it.toString())
                        val item = Photo(null, null, it.toString())

//                        viewModel.addPhoto(item)
                        adapter.addItem(item)
                        return
                    }

                    Log.d(DEBUG_TAG, "" + UCrop.getOutput(data!!))
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        }
    }

    private fun startCropActivity(uri: Uri) {
        val mDestinationUri = Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}.jpg"))

        val uCrop = UCrop.of(uri, mDestinationUri).withAspectRatio(1f, 1f).withMaxResultSize(1080, 1080)
        val options: UCrop.Options = UCrop.Options()

        options.setShowCropFrame(false)
        options.setShowCropGrid(false)
        options.setHideBottomControls(true)
        options.setActiveControlsWidgetColor(getColor(R.color.primary))
        options.setFreeStyleCropEnabled(false)
        uCrop.withOptions(options)

        uCrop.start(this)
    }

    private fun savePhotos(){
        val gson = Gson()
        val arrayPhotoType = object : TypeToken<ArrayList<Photo>>() {}.type
        val itemsJson : String = gson.toJson(adapter.items, arrayPhotoType)

        val data = Intent()
        data.putExtra("photo-items", itemsJson)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}


//Legacy
//    private fun isMatchedDate(takenDate: String): Boolean {
////        return selectedDate == takenDate
//    }
//
//    private fun isWritable(): Boolean {
////        return pagerAdapter.count < 5
//    }

//private fun convertDate(date: String): String {
//    val month = if (date.substring(5, 7).toInt() < 10) {
//        date.substring(6, 7)
//    } else {
//        date.substring(5, 7)
//    }
//
//    val day = if (date.substring(8, 10).toInt() < 10) {
//        date.substring(9, 10)
//    } else {
//        date.substring(8, 10)
//    }
//
//    val hour = if (date.substring(11, 13).toInt() < 12) {
//        "AM ${date.substring(12, 13)}"
//    } else {
//        "PM ${date.substring(11, 13).toInt() - 12}"
//    }
//
//    val minute = if (date.substring(14, 16).toInt() < 10) {
//        date.substring(15, 16)
//    } else {
//        date.substring(14, 16)
//    }
//
//    return "${hour}시 ${minute}분 ${month}월 ${day}일"
//}

//private fun bitmapToUri(bitmap: Bitmap): Uri? {
//    val imageFile = File(cacheDir, "${UUID.randomUUID()}.jpg")
//    val os: OutputStream
//    return try {
//        os = FileOutputStream(imageFile)
//        bitmap.compress(CompressFormat.JPEG, 100, os)
//        os.flush()
//        os.close()
//        imageFile.toUri()
//    } catch (e: Exception) {
//        binding.choicePhotoRootLayout.snackbar("사진을 불러오는데 실패하였습니다.")
//        null
//        //Log.e(javaClass.simpleName, "Error writing bitmap", e)
//    }
//}

//private fun exifOrientationToDegree(exifOrientation: Int): Int {
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
////            binding.writeDiaryRootLayout.snackbar("Error: " + e.message)
//        }
//
//        return photo
//    }

//draw text on photo
//private fun drawTextToBitmap(photo: Photo): Bitmap? {
//    return try {
//        val bitmap: Bitmap = photo.bitmap!!
//        val scale: Float = resources.displayMetrics.density
//        var config: Bitmap.Config? = bitmap.config
//
//        if (config == null) {
//            config = Bitmap.Config.ARGB_8888
//        }
//
//        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
//        val newCanvas = Canvas(newBitmap)
//
//        newCanvas.drawBitmap(bitmap, 0f, 0f, null)
//
//        val captionString = convertDate(photo.date!!)
//
//        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
//        paintText.color = ContextCompat.getColor(this, R.color.white)
//        paintText.textSize = 100f
//        paintText.typeface = Typeface.createFromAsset(assets, "notosanskr_bold.otf")
//
//        val rectText = Rect()
//        paintText.getTextBounds(captionString, 0, captionString.length, rectText)
//
//        val y = ((newBitmap.height + rectText.height()) / 3) * scale
//        newCanvas.drawText(
//            captionString,
//            100f, y.toFloat(), paintText
//        )
//        newBitmap
//    } catch (e: FileNotFoundException) {
////            binding.writeDiaryRootLayout.snackbar("Error: " + e.message)
//        null
//    }
//}


//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        imageUri = getImageUri();
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intent, REQUEST_TAKE_PICTURE);

//        if(intent.resolveActivity(packageManager) != null){
//            startActivityForResult(intent, REQUEST_TAKE_PICTURE)
//        }


//                    val extras = data!!.extras
//
//                    val imageBitmap = extras!!["data"] as Bitmap?
//
//                    bitmapToUri(imageBitmap!!)?.let {
//                        //                    Log.d("ChoicePhotoDebug://", bitmapToUri(imageBitmap!!).toString())
//                        startCropActivity(it)
//                        return
//                    }
//
//                    binding.choicePhotoRootLayout.snackbar("사진을 불러오는데 실패했습니다.")