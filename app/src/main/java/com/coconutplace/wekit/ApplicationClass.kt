package com.coconutplace.wekit

import android.app.Application
import com.coconutplace.wekit.di.networkModule
import com.coconutplace.wekit.di.repositoryModule
import com.coconutplace.wekit.di.utilityModule
import com.coconutplace.wekit.di.viewModelModule
import com.coconutplace.wekit.utils.MyFirebaseMessagingService
import com.coconutplace.wekit.utils.PushUtil
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.BuildConfig.DEBUG
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ApplicationClass : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin{
            if (DEBUG) {
                androidLogger()
            } else {
            androidLogger(Level.NONE)
        }
            androidContext(this@ApplicationClass)
            modules(
                utilityModule,
                viewModelModule,
                networkModule,
                repositoryModule
            )
        }

        PushUtil.registerPushHandler(MyFirebaseMessagingService())

    }
}