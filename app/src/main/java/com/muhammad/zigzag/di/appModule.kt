package com.muhammad.zigzag.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.muhammad.zigzag.data.local.AppDatabase
import com.muhammad.zigzag.data.local.getRoomDatabase
import com.muhammad.zigzag.data.repository.PathRepositoryImpl
import com.muhammad.zigzag.data.repository.SettingRepositoryImp
import com.muhammad.zigzag.data.repository.WhiteBoardRepositoryImp
import com.muhammad.zigzag.domain.repository.PathRepository
import com.muhammad.zigzag.domain.repository.SettingRepository
import com.muhammad.zigzag.domain.repository.WhiteBoardRepository
import com.muhammad.zigzag.presentation.screens.home.HomeViewModel
import com.muhammad.zigzag.presentation.screens.settings.SettingsViewModel
import com.muhammad.zigzag.presentation.screens.whiteboard.WhiteboardViewModel

val appModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().pathDao() }
    single { get<AppDatabase>().whiteBoardDao() }
    singleOf(::PathRepositoryImpl).bind<PathRepository>()
    singleOf(::SettingRepositoryImp).bind<SettingRepository>()
    singleOf(::WhiteBoardRepositoryImp).bind<WhiteBoardRepository>()
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::WhiteboardViewModel)
}