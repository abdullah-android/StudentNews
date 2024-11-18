@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.android.studentnews.main.news.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.android.studentnews.core.data.snackbar_controller.SnackBarController
import com.android.studentnews.core.data.snackbar_controller.SnackBarEvents
import com.android.studentnews.core.domain.common.isInternetAvailable
import com.android.studentnews.core.domain.constants.FontSize
import com.android.studentnews.core.ui.common.ButtonColors
import com.android.studentnews.main.news.domain.destination.NewsDestination
import com.android.studentnews.main.news.ui.viewModel.NewsDetailViewModel
import com.android.studentnews.news.domain.model.NewsModel
import com.android.studentnews.news.domain.model.UrlList
import com.android.studentnews.news.ui.viewModel.NewsViewModel
import com.android.studentnews.ui.theme.Black
import com.android.studentnews.ui.theme.DarkGray
import com.android.studentnews.ui.theme.Gray
import com.android.studentnews.ui.theme.Green
import com.android.studentnews.ui.theme.Red
import com.android.studentnews.ui.theme.White
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@UnstableApi
@Composable
fun SharedTransitionScope.NewsDetailScreen(
    newsId: String,
    navHostController: NavHostController,
    newsDetailViewModel: NewsDetailViewModel,
    newsViewModel: NewsViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val newsById by newsDetailViewModel.newsById.collectAsStateWithLifecycle()
    val savedNewsById by newsDetailViewModel.savedNewsById.collectAsStateWithLifecycle()
    val currentUser by newsViewModel.currentUser.collectAsStateWithLifecycle()

    var isSaved by remember(savedNewsById) {
        mutableStateOf(savedNewsById != null)
    }
    var isLiked by remember(newsById, currentUser) {
        mutableStateOf(
            newsById?.likes?.contains(currentUser?.uid ?: "") ?: false
        )
    }
    var isShareBtnClicked by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        pageCount = {
            newsById?.urlList?.size ?: 1
        }
    )

    LaunchedEffect(Unit) {
        newsDetailViewModel.getNewsById(newsId)
        newsDetailViewModel.getSavedNewsById(newsId)
    }

    LaunchedEffect(isShareBtnClicked) {
        if (isShareBtnClicked) {
            val title = newsById?.title ?: ""
            val imageUrl = getUrlOfImageNotVideo(
                newsById?.urlList ?: emptyList()
            )

            newsDetailViewModel.onShareNews(
                imageUrl,
                context,
                onShare = { fileUri ->
                    Intent(
                        Intent.ACTION_SEND,
                    ).apply {
                        if (fileUri != null) {
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        putExtra(Intent.EXTRA_TEXT, title)
                        type = "text/plain"
                    }.let { intent ->
                        val sharedIntent = Intent.createChooser(
                            intent,
                            null,
                        )

                        context.startActivity(sharedIntent)
                        newsDetailViewModel.storeShareCount(newsId)
                    }
                }
            )
        }
    }


    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSystemInDarkTheme()) Color.Black else White
                        )
                        .padding(all = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Icon for Navigate Back"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row() {

                        // Like
                        IconButton(
                            onClick = {
                                isLiked = !isLiked

                                if (isLiked) {
                                    newsDetailViewModel.onNewsLike(newsId)
                                } else {
                                    newsDetailViewModel.onNewsUnLike(newsId)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isLiked) Red else {
                                    if (isSystemInDarkTheme()) White else Black
                                }
                            ),
                        ) {

                            AnimatedVisibility(isLiked) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Icon of liked News",
                                )
                            }

                            AnimatedVisibility(!isLiked) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = "Icon of unliked News",
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isInternetAvailable(context)) {

                                    isSaved = !isSaved

                                    newsById?.let {
                                        if (isSaved) {
                                            val news = NewsModel(
                                                newsId = it.newsId,
                                                title = it.title,
                                                description = it.description,
                                                category = it.category,
                                                timestamp = Timestamp.now(),
                                                link = it.link,
                                                linkTitle = it.linkTitle,
                                                urlList = it.urlList,
                                                shareCount = it.shareCount ?: 0
                                            )

                                            newsViewModel.viewModelScope.launch {
                                                newsViewModel
                                                    .onNewsSave(news)
                                                    .collect { result ->
                                                        when (result) {
                                                            else -> {}
                                                        }
                                                    }
                                            }
                                        } else {
                                            newsViewModel.onNewsRemoveFromSave(
                                                it.newsId ?: "",
                                                wantToShowSnackBar = false
                                            )
                                        }
                                    }

                                } else {
                                    scope.launch {
                                        SnackBarController
                                            .sendEvent(
                                                SnackBarEvents(
                                                    message = "No Internet Connection!",
                                                    duration = SnackbarDuration.Long
                                                )
                                            )
                                    }
                                }
                            },
                        ) {
                            AnimatedVisibility(isSaved) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Icon for Saved News",
                                )
                            }

                            AnimatedVisibility(!isSaved) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkAdd,
                                    contentDescription = "Icon for unSaved News",
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isShareBtnClicked = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Icon for unSaved News",
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateLeftPadding(LayoutDirection.Rtl),
                )
                .verticalScroll(scrollState)
                .sharedElement(
                    state = rememberSharedContentState(key = "container/$newsId"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
        ) {

            Box {

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                ) { page ->

                    val item = newsById?.urlList?.get(page)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        if (item?.contentType.toString().startsWith("image")) {

                            val imageRequest = ImageRequest.Builder(context)
                                .data(item?.url ?: "")
                                .crossfade(true)
                                .build()

                            SubcomposeAsyncImage(
                                model = imageRequest,
                                contentDescription = "Image",
                                contentScale = ContentScale.Fit,
                                loading = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        CircularProgressIndicator(color = Green)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .sharedElement(
                                        state = rememberSharedContentState(key = "image/$newsId"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    ),
                            )

                        }

                        if (item?.contentType.toString().startsWith("video")) {

                            val mediaItem = MediaItem.Builder()
                                .setUri(item?.url)
                                .build()

                            val exoplayer = remember(context, mediaItem) {
                                ExoPlayer.Builder(context)
                                    .build()
                                    .apply {
                                        setMediaItem(mediaItem)
                                        prepare()
                                        // playWhenReady = true
                                    }
                            }

                            var isPlaying by remember { mutableStateOf(false) }

                            DisposableEffect(
                                Box {
                                    AndroidView(
                                        factory = {
                                            PlayerView(it).apply {
                                                player = exoplayer
                                                useController = true
                                                imageDisplayMode =
                                                    PlayerView.IMAGE_DISPLAY_MODE_FILL
                                                hideController()
                                                setShowFastForwardButton(false)
                                                setShowRewindButton(false)
                                                setShowNextButton(false)
                                                setShowPreviousButton(false)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(
                                                state = rememberSharedContentState(key = "image/$newsId"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                            ),
                                    )

                                    // Video Play Icon
                                    if (!isPlaying) {
                                        IconButton(
                                            onClick = {
                                                exoplayer.play()
                                            },
                                            modifier = Modifier
                                                .background(
                                                    color = White,
                                                    shape = CircleShape
                                                )
                                                .align(Alignment.Center)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Icon for Playing the Video",
                                                tint = Black
                                            )
                                        }
                                    }
                                }
                            ) {
                                onDispose {
                                    exoplayer.release()
                                    isPlaying = false
                                }
                            }

                            val listener = remember {
                                object : Player.Listener {
                                    override fun onIsPlayingChanged(myIsPlaying: Boolean) {
                                        if (myIsPlaying) {
                                            isPlaying = true
                                        }
                                    }
                                }
                            }

                            exoplayer.addListener(listener)

                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Black.copy(0.3f)
                            ),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(all = 5.dp)
                            ) {
                                Text(text = "${pagerState.currentPage + 1}", color = White)
                                Text(text = "/", color = White)
                                Text(text = "${pagerState.pageCount}", color = White)
                            }
                        }
                    }

                }

            }

            Box {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(bottom = 20.dp, end = 10.dp)
                ) {

                    // Share
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Icon
                        IconButton(
                            onClick = {
                                isShareBtnClicked = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Icon of unliked News",
                            )
                        }
                        // Share Count
                        if ((newsById?.shareCount ?: 0) > 0) {
                            Text(text = (newsById?.shareCount ?: 0).toString())
                        }
                    }

                    // Save
                    IconButton(
                        onClick = {
                            isSaved = !isSaved

                            if (isSaved) {
                                val news = NewsModel(
                                    newsId = newsById?.newsId ?: "",
                                    title = newsById?.title ?: "",
                                    description = newsById?.description ?: "",
                                    category = newsById?.category ?: "",
                                    timestamp = Timestamp.now(),
                                    link = newsById?.link ?: "",
                                    linkTitle = newsById?.linkTitle ?: "",
                                    urlList = newsById?.urlList ?: emptyList(),
                                    shareCount = newsById?.shareCount ?: 0,
                                    likes = newsById?.likes ?: emptyList()
                                )

                                scope.launch {
                                    newsViewModel.onNewsSave(news).collect { result ->
                                        when (result) {
                                            else -> {}
                                        }
                                    }
                                }
                            } else {
                                newsViewModel.onNewsRemoveFromSave(newsId, false)
                            }
                        },
                    ) {
                        this@Column.AnimatedVisibility(isSaved) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Icon of Liked News",
                            )
                        }

                        this@Column.AnimatedVisibility(!isSaved) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Icon of unliked News",
                            )
                        }
                    }

                    // Like
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = "like/$newsId"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                renderInOverlayDuringTransition = true
                            ),
                    ) {
                        // Like Icon
                        IconButton(
                            onClick = {
                                isLiked = !isLiked

                                if (isLiked) {
                                    newsDetailViewModel.onNewsLike(newsId)
                                } else {
                                    newsDetailViewModel.onNewsUnLike(newsId)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isLiked) Red else {
                                    if (isSystemInDarkTheme()) White else Black
                                }
                            ),
                        ) {
                            this@Column.AnimatedVisibility(isLiked) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Icon of Liked News",
                                )
                            }

                            this@Column.AnimatedVisibility(!isLiked) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = "Icon of unliked News",
                                )
                            }
                        }
                        // Like Count
                        this@Column.AnimatedVisibility(
                            isLiked && (newsById?.likes?.size ?: 0) > 0
                        ) {
                            Text(text = (newsById?.likes?.size ?: 0).toString())
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp)
                        .background(
                            color = Green.copy(0.1f)/*LightGray.copy(0.3f)*/,
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp,
                            )
                        ),
                ) {
                    // Category Container
                    Box(
                        modifier = Modifier
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 20.dp,
                                bottom = 5.dp
                            )
                            .background(
                                color = Black.copy(0.1f),
                                shape = RoundedCornerShape(5.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = newsById?.category ?: "",
                            style = TextStyle(
                                fontSize = FontSize.MEDIUM.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            ),
                            modifier = Modifier
                                .padding(all = 5.dp)
                        )
                    }

                    val customLineBreak = LineBreak(
                        strategy = LineBreak.Strategy.HighQuality,
                        strictness = LineBreak.Strictness.Strict,
                        wordBreak = LineBreak.WordBreak.Phrase
                    )

                    Text(
                        text = newsById?.title ?: "",
                        style = TextStyle(
                            fontSize = FontSize.LARGE.sp,
                            fontWeight = FontWeight.Bold,
                            lineBreak = customLineBreak,
                            hyphens = Hyphens.Auto,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 20.dp,
                                bottom = 10.dp
                            )
                            .sharedElement(
                                state = rememberSharedContentState(key = "title/$newsId"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                    )

                    SelectionContainer {
                        Text(
                            text = newsById?.description ?: "",
                            style = TextStyle(
                                fontSize = FontSize.MEDIUM.sp,
                                lineBreak = customLineBreak,
                                hyphens = Hyphens.Auto,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 20.dp)
                        )
                    }

                    if (!newsById?.link.isNullOrEmpty()) {
                        if (!newsById?.link.isNullOrEmpty()) {

                            Spacer(modifier = Modifier.height(10.dp))

                            if (newsById?.link.toString().toUri().isAbsolute) {
                                FilledTonalButton(
                                    onClick = {
                                        navHostController.navigate(
                                            NewsDestination.NEWS_LINK_SCREEN(
                                                link = newsById?.link ?: ""
                                            )
                                        )
                                    },
                                    colors = ButtonColors(
                                        containerColor = Green.copy(0.5f),
                                        contentColor = White
                                    ),
                                    modifier = Modifier
                                        .padding(all = 20.dp)
                                ) {
                                    Text(text = newsById?.linkTitle ?: "")
                                }
                            }
                        }
                    }
                }

            }

        }
    }
}

fun getUrlOfImageNotVideo(urlList: List<UrlList?>): String {
    var imageIndex = mutableIntStateOf(0)
    val imageUrl =
        if (
            urlList.get(imageIndex.intValue)
                ?.contentType.toString().startsWith("image/")
        ) urlList.get(imageIndex.intValue)?.url ?: ""
        else urlList.get(imageIndex.intValue++)?.url ?: ""

    return imageUrl
}
