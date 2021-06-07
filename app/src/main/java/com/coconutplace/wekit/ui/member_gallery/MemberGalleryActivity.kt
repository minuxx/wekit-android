package com.coconutplace.wekit.ui.member_gallery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.PhotoPack
import com.coconutplace.wekit.data.remote.gallery.listeners.GalleryListener
import com.coconutplace.wekit.databinding.ActivityMemberGalleryBinding
import com.coconutplace.wekit.ui.photo_viewer.PhotoViewerActivity
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.snackbar
import kotlinx.android.synthetic.main.activity_member_gallery.*

import org.koin.androidx.viewmodel.ext.android.viewModel

class MemberGalleryActivity :AppCompatActivity(),GalleryListener {
    private lateinit var mBinding:ActivityMemberGalleryBinding
    private lateinit var galleryListAdapter: PhotoPackListAdapter
    private val mMemberGalleryViewModel : MemberGalleryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userIdx = intent.getIntExtra("userIdx",-1)
        val roomIdx = intent.getIntExtra("roomIdx",-1)
        val nickName = intent.getStringExtra("nickName")

        Log.e(CHECK_TAG,"userIdx : $userIdx, roomIdx : $roomIdx")

        setupView()
        setupPhotoPackListAdapter()
        setupViewModel(userIdx, roomIdx, nickName!!)
    }

    private fun setupView(){
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_member_gallery)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mMemberGalleryViewModel

        mBinding.galleryBackButton.setOnClickListener {
            finish()
        }
        mBinding.galleryBackButtonFrameLayout.setOnClickListener{
            finish()
        }

        gallery_recyclerview.layoutManager = LinearLayoutManager(this)

        mBinding.galleryRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if ((gallery_recyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == galleryListAdapter.itemCount-1) {
                    //Log.e(CHECK_TAG,"가장 오래된 메세지를 보았습니다.")
                    mMemberGalleryViewModel.getMemberGallery()
                }
            }
        })
    }

    private fun setupPhotoPackListAdapter(){
        mBinding.galleryRecyclerview.layoutManager = LinearLayoutManager(this)
        galleryListAdapter = PhotoPackListAdapter()
        mBinding.galleryRecyclerview.adapter = galleryListAdapter

        galleryListAdapter.setItemClickListener(object: PhotoPackListAdapter.OnItemClickListener{
            override fun onClick(url:String, type:String?) {
                //Log.e(CHECK_TAG,"on clicked url : $url, type : $type")
                val intent = Intent(this@MemberGalleryActivity, PhotoViewerActivity::class.java)
                intent.putExtra("url", url)
                if(type!=null){
                    intent.putExtra("type", type)
                }
                startActivity(intent)
            }
        })
    }

    private fun setupViewModel(userIdx:Int, roomIdx:Int, nickName:String){
        mMemberGalleryViewModel.setGalleryListener(this)
        mMemberGalleryViewModel.setIndex(userIdx, roomIdx, nickName)
        mMemberGalleryViewModel.setPage(1)
        mMemberGalleryViewModel.setIsDone(false)
        mMemberGalleryViewModel.getMemberGallery()
    }

    override fun addPhotoPack(photoPack: PhotoPack) {
        runOnUiThread{
            galleryListAdapter.addLast(photoPack)
        }
    }

    override fun makeSnackBar(str: String) {
        mBinding.root.snackbar(str)
    }
}