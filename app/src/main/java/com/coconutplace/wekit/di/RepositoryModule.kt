package com.coconutplace.wekit.di

import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.data.repository.badge.BadgeRepository
import com.coconutplace.wekit.data.repository.body.BodyRepository
import com.coconutplace.wekit.data.repository.diary.DiaryRepository
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
import com.coconutplace.wekit.data.repository.chat.ChatRepository
import com.coconutplace.wekit.data.repository.gallery.GalleryRepository
import com.coconutplace.wekit.data.repository.home.HomeRepository
import com.coconutplace.wekit.data.repository.notice.NoticeRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get()) }
    single { HomeRepository(get()) }
    single { DiaryRepository(get()) }
    single { ChannelRepository(get()) }
    single { ChatRepository(get()) }
    single { BodyRepository(get()) }
    single { NoticeRepository(get()) }
    single { GalleryRepository(get()) }
    single { BadgeRepository(get()) }
}