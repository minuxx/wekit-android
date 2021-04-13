package com.coconutplace.wekit.di

import com.coconutplace.wekit.utils.GlobalConstant
import com.coconutplace.wekit.utils.SharedPreferencesManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val utilityModule = module {
    single { SharedPreferencesManager(androidContext()) }
    single { GlobalConstant() }
}