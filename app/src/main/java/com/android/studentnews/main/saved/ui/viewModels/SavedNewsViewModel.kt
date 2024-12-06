package com.android.studentnews.main.settings.saved.ui.viewModels

import android.net.ipsec.ike.exceptions.IkeInternalException
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.studentnews.core.data.snackbar_controller.SnackBarActions
import com.android.studentnews.core.data.snackbar_controller.SnackBarController
import com.android.studentnews.core.data.snackbar_controller.SnackBarEvents
import com.android.studentnews.core.domain.constants.Status
import com.android.studentnews.news.domain.model.NewsModel
import com.android.studentnews.news.domain.repository.NewsRepository
import com.android.studentnews.news.domain.resource.NewsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedNewsViewModel(
    private val newsRepository: NewsRepository
): ViewModel() {

    private val _savedNewsList = MutableStateFlow<List<NewsModel>>(emptyList())
    val savedNewsList = _savedNewsList.asStateFlow()

    var savedNewsStatus by mutableStateOf("")
        private set

    var newsRemoveFromSaveStatus by mutableStateOf("")
        private set


    init {
        getSavedNewsList()
    }


    fun onNewsRemoveFromSave(news: NewsModel) {
        viewModelScope.launch {
            newsRemoveFromSaveStatus = Status.Loading
            newsRepository
                .onNewsRemoveFromSave(news)
                .collectLatest { result ->
                    when (result) {
                        is NewsState.Success -> {
                            newsRemoveFromSaveStatus = Status.SUCCESS
                            SnackBarController
                                .sendEvent(
                                    SnackBarEvents(
                                        message = result.data,
                                        duration = SnackbarDuration.Long,
                                        action = SnackBarActions(
                                            label = "Undo",
                                            action = {
                                                onNewsRemoveFromSaveUndo(news)
                                            }
                                        )
                                    )
                                )
                        }

                        is NewsState.Failed -> {
                            newsRemoveFromSaveStatus = Status.FAILED
                            SnackBarController
                                .sendEvent(
                                    SnackBarEvents(
                                        message = result.error.localizedMessage ?: "",
                                        duration = SnackbarDuration.Long
                                    )
                                )
                        }

                        else -> {}
                    }
                }
        }
    }

    fun onNewsRemoveFromSaveUndo(news: NewsModel) {
        viewModelScope.launch {
            newsRepository
                .onNewsSave(news)
                .collectLatest { result ->
                    when (result) {
                        is NewsState.Failed -> {
                            SnackBarController
                                .sendEvent(
                                    SnackBarEvents(
                                        message = result.error.localizedMessage ?: "",
                                        duration = SnackbarDuration.Long
                                    )
                                )
                        }

                        else -> {}
                    }
                }
        }
    }

    fun getSavedNewsList() {
        viewModelScope.launch {
            newsRepository
                .getSavedNewsList()
                .collectLatest { result ->
                    when (result) {
                        is NewsState.Loading -> {
                            savedNewsStatus = Status.Loading
                        }

                        is NewsState.Success -> {
                            _savedNewsList.value = result.data
                            savedNewsStatus = Status.SUCCESS
                        }

                        is NewsState.Failed -> {
                            SnackBarController
                                .sendEvent(
                                    SnackBarEvents(
                                        message = result.error.localizedMessage ?: "",
                                        duration = SnackbarDuration.Long
                                    )
                                )
                        }
                    }
                }
        }
    }

}