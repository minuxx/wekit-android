package com.coconutplace.wekit.data.repository.diary

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryResponse
import com.coconutplace.wekit.data.remote.diary.DiaryService
import com.coconutplace.wekit.data.repository.BaseRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DiaryRepository(private val diaryService: DiaryService) : BaseRepository() {
    private val executor: Executor = Executors.newFixedThreadPool(5)

    suspend fun getDiaries(date: String, page: Int): DiaryResponse {
        return apiRequest { diaryService.getDiaries(date, page) }
    }

    suspend fun postDiary(diary: Diary): DiaryResponse {
        return apiRequest { diaryService.postDiary(diary) }
    }

    suspend fun getDiary(diaryIdx: Int): DiaryResponse {
        return apiRequest { diaryService.getDiary(diaryIdx) }
    }

    suspend fun getWrittenDates(date: String): DiaryResponse {
        return apiRequest { diaryService.getWrittenDates(date) }
    }

//     fun getDiaries(date: String, page: Int){
//         val dataSourceFactory = DiaryDataFactory(date, diaryService)
//         val pagedListConfig = PagedList.Config.Builder()
//             .setPageSize(10)
//             .setInitialLoadSizeHint(10)
//             .setPrefetchDistance(10)
//             .setEnablePlaceholders(false)
//             .build()
//
//         val data = LivePagedListBuilder(dataSourceFactory, pagedListConfig)
//             .setFetchExecutor(executor)
//             .build()
//
//         val networkErrors = Transformations.switchMap(dataSourceFactory.mutableLiveData
//         ) { dataSource -> dataSource.networkErrors }
//
//         return DiaryResultV2(data, networkErrors)
//    }
}