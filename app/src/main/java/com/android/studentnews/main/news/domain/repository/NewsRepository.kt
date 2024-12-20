package com.android.studentnews.news.domain.repository

import androidx.paging.PagingData
import com.android.studentnews.main.news.domain.model.CategoryModel
import com.android.studentnews.news.domain.model.NewsModel
import com.android.studentnews.news.domain.resource.NewsState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    // References
    val userDocRef: DocumentReference?
    val newsColRef: CollectionReference?
    val categoriesColRef: CollectionReference?
    val savedNewsColRef: CollectionReference?

    // News
    fun getNewsList(category: String?): Flow<PagingData<NewsModel>>
    suspend fun getNewsUpdates(): NewsModel?
    fun getSavedNewsList(limit: Int): Flow<PagingData<NewsModel>>
    // Liked News
    fun getLikedNewsList(limit: Int): Flow<PagingData<NewsModel>>

    // Category
    fun getCategoriesList(limit: Int): Flow<PagingData<CategoryModel>>

    // Search
    fun onSearch(
        query: String,
        currentSelectedCategory: String?,
        limit: Int,
    ): Flow<PagingData<NewsModel>>

    fun setupPeriodicNewsWorkRequest()
    fun cancelPeriodicNewsWorkRequest()

}