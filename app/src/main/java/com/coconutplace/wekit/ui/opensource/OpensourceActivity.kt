package com.coconutplace.wekit.ui.opensource

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.License
import com.coconutplace.wekit.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_opensource.*

class OpensourceActivity : BaseActivity(){
    private lateinit var mAdapter: OpensourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opensource)
        opensource_back_btn.setOnClickListener(this)

        initRecyclerView()

        opensource_license_mit_content_tv.text = HtmlCompat.fromHtml(getString(R.string.opensource_mit_license_content), HtmlCompat.FROM_HTML_MODE_LEGACY)
        opensource_license_apache_content_tv.text = HtmlCompat.fromHtml(getString(R.string.opensource_apache_license_content), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            opensource_back_btn -> finish()
        }
    }

    private fun initRecyclerView(){
        mAdapter = OpensourceAdapter(this)

        opensource_recyclerview.adapter = mAdapter

        val licenses: ArrayList<License> = ArrayList()

        licenses.add(License("lottie-android", "https://github.com/airbnb/lottie-android", "Copyright 2018 Airbnb, Inc.", "Apache License 2.0"))
        licenses.add(License("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart/blob/master/LICENSE", "Copyright 2020 Philipp Jahoda", "Apache License 2.0"))
        licenses.add(License("PageIndicatorView", "https://github.com/romandanylyk/PageIndicatorView", "Copyright 2017 Roman Danylyk", "Apache License 2.0"))
        licenses.add(License("RealtimeBlurView", "https://github.com/mmin18/RealtimeBlurView", "Copyright 2016 Tu Yimin (http://github.com/mmin18)", "Apache License 2.0"))
        licenses.add(License("glide", "https://github.com/bumptech/glide/blob/master/LICENSE", "Copyright 2014 Google, Inc. All rights reserved.\nCopyright 2012 Jake Wharton\nCopyright 2011 The Android Open Source Project\nCopyright (c) 2013 Xcellent Creations, Inc.\nCopyright (c) 1994 Anthony Dekker", "Apache License 2.0"))
        licenses.add(License("material-calendarview", "https://github.com/prolificinteractive/material-calendarview", "Copyright (c) 2018 Prolific Interactive", "MIT License"))
        licenses.add(License("AndroidTagView", "https://github.com/whilu/AndroidTagView", "Copyright 2015 lujun", "Apache License 2.0"))
        licenses.add(License("PhotoView", "https://github.com/Baseflow/PhotoView", "Copyright 2018 Chris Banes", "Apache License 2.0"))
        licenses.add(License("TedPermission", "https://github.com/ParkSangGwon/TedPermission", "Copyright 2017 Ted Park", "Apache License 2.0"))
        licenses.add(License("uCrop", "https://github.com/Yalantis/uCrop", "Copyright 2017, Yalantis", "Apache License 2.0"))

        mAdapter.addItems(licenses)
    }
}