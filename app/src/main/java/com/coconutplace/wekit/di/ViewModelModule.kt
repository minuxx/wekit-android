package com.coconutplace.wekit.di

import com.coconutplace.wekit.ui.badge.BadgeViewModel
import com.coconutplace.wekit.ui.body_graph.BodyGraphViewModel
import com.coconutplace.wekit.ui.channel.ChannelViewModel
import com.coconutplace.wekit.ui.channel_filter.ChannelFilterViewModel
import com.coconutplace.wekit.ui.chat.ChatViewModel
import com.coconutplace.wekit.ui.choice_photo.ChoiceViewModel
import com.coconutplace.wekit.ui.create_channel.CreateChannelViewModel
import com.coconutplace.wekit.ui.diary.DiaryViewModel
import com.coconutplace.wekit.ui.edit_password.EditPasswordViewModel
import com.coconutplace.wekit.ui.enter_channel.EnterChannelViewModel
import com.coconutplace.wekit.ui.home.HomeViewModel
import com.coconutplace.wekit.ui.input_body.InputBodyViewModel
import com.coconutplace.wekit.ui.login.LoginViewModel
import com.coconutplace.wekit.ui.main.MainViewModel
import com.coconutplace.wekit.ui.member_gallery.MemberGalleryViewModel
import com.coconutplace.wekit.ui.notice.NoticeViewModel
import com.coconutplace.wekit.ui.poll.PollViewModel
import com.coconutplace.wekit.ui.profile.ProfileViewModel
import com.coconutplace.wekit.ui.set.SetViewModel
import com.coconutplace.wekit.ui.signup.SignUpViewModel
import com.coconutplace.wekit.ui.splash.SplashViewModel
import com.coconutplace.wekit.ui.write_diary.WriteDiaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { PollViewModel(get(), get()) }
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { DiaryViewModel(get()) }
    viewModel { WriteDiaryViewModel(get(), get()) }
    viewModel { ChoiceViewModel() }
    viewModel { CreateChannelViewModel(get()) }
    viewModel { ChannelViewModel(get(), get()) }
    viewModel { EnterChannelViewModel(get()) }
    viewModel { ChatViewModel(get(), get()) }
    viewModel { BodyGraphViewModel(get()) }
    viewModel { InputBodyViewModel(get(), get()) }
    viewModel { SetViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { EditPasswordViewModel(get(), get()) }
    viewModel { NoticeViewModel(get()) }
    viewModel { MemberGalleryViewModel(get()) }
    viewModel { ChannelFilterViewModel() }
    viewModel { BadgeViewModel(get(),get()) }
}