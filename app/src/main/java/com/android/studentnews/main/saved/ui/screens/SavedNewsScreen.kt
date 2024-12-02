@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.studentnews.main.settings.saved.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.studentnews.core.domain.common.isInternetAvailable
import com.android.studentnews.core.domain.constants.FontSize
import com.android.studentnews.core.domain.constants.Status
import com.android.studentnews.core.ui.common.LoadingDialog
import com.android.studentnews.main.news.domain.destination.NewsDestination
import com.android.studentnews.main.news.ui.screens.getUrlOfImageNotVideo
import com.android.studentnews.main.settings.saved.ui.viewModels.SavedNewsViewModel
import com.android.studentnews.news.domain.model.NewsModel
import com.android.studentnews.ui.theme.Black
import com.android.studentnews.ui.theme.DarkColor
import com.android.studentnews.ui.theme.DarkGray
import com.android.studentnews.ui.theme.Gray
import com.android.studentnews.ui.theme.LightGray
import com.android.studentnews.ui.theme.Red
import com.android.studentnews.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SavedNewsScreen(
    navHostController: NavHostController,
    savedNewsViewModel: SavedNewsViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) {

    val context = LocalContext.current
    val density = LocalDensity.current


    val savedNewsList by savedNewsViewModel.savedNewsList.collectAsStateWithLifecycle()


    Surface(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (savedNewsList.size != 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                items(
                    count = savedNewsList.size,
                    key = { index ->
                        savedNewsList[index].newsId ?: ""
                    }
                ) { index ->
                    val item = savedNewsList[index]
                    SavedNewsItem(
                        item = item,
                        context = context,
                        density = density,
                        onItemClick = { newsId ->
                            navHostController.navigate(NewsDestination.NEWS_DETAIL_SCREEN(newsId))
                        },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope,
                        onRemoveFromSavedList = { thisNews ->
                            savedNewsViewModel.onNewsRemoveFromSave(thisNews)
                        },
                    )
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(text = "No Saved News")
            }
        }


        if (savedNewsViewModel.savedNewsStatus == Status.Loading) {
            LoadingDialog()
        }

    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SavedNewsItem(
    item: NewsModel?,
    context: Context,
    density: Density,
    onItemClick: (String) -> Unit,
    onRemoveFromSavedList: (NewsModel) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) {

    var offsetX = remember { Animatable(0f) }
    var scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var maxWidth = 250.dp
    var itemHeight by remember { mutableStateOf(0.dp) }


    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            if (isDragging) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .background(color = Black)
                ) {
                    AnimatedContent(
                        targetState = (offsetX.value).dp > maxWidth
                                || (-offsetX.value).dp > maxWidth,
                        label = "delete_from_save",
                        modifier = Modifier
                            .align(
                                if ((offsetX.value.toString()).startsWith("-"))
                                    Alignment.CenterEnd else Alignment.CenterStart
                            ),
                    ) { targetState ->
                        Icon(
                            imageVector = if (targetState)
                                Icons.Default.Delete else Icons.Outlined.Delete,
                            contentDescription = "Icon for Remove Item from Saved List",
                            tint = if (targetState) Red else White,
                        )
                    }
                }
            }

            with(sharedTransitionScope) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onItemClick.invoke(item?.newsId ?: "")
                        }
                        .sharedElement(
                            state = rememberSharedContentState(key = "container/${item?.newsId}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                (offsetX.value).roundToInt(),
                                0
                            )
                        }
                        .pointerInput(true) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    if ((offsetX.value).dp > maxWidth || (-offsetX.value).dp > maxWidth) {
                                        item?.let { thisItem ->
                                            scope.launch {
                                                if ((offsetX.value.toString()).startsWith("-")) {
                                                    offsetX.animateTo(offsetX.value + -500f)
                                                } else {
                                                    offsetX.animateTo(offsetX.value + 500f)
                                                }
                                                delay(100)
                                                onRemoveFromSavedList.invoke(thisItem)
                                            }
                                        }
                                    } else {
                                        isDragging = false
                                        scope.launch {
                                            offsetX.animateTo(0f)
                                        }
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    val newOffsetX = dragAmount
                                    val incrementedOffsetX = (offsetX.value) + newOffsetX
                                    scope.launch {
                                        offsetX.snapTo(incrementedOffsetX)
                                    }
                                }
                            )
                        }
                        .background(
                            color = if (isDragging) {
                                if (isSystemInDarkTheme()) DarkGray else LightGray
                            } else {
                                if (isSystemInDarkTheme()) DarkColor else White
                            }
                        )
                        .onGloballyPositioned { coordinates ->
                            itemHeight = with(density) { coordinates.size.height.toDp() }
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        color = Black.copy(0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    text = item?.category ?: "",
                                    modifier = Modifier
                                        .padding(all = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item?.title ?: "",
                                style = TextStyle(
                                    fontSize = (FontSize.MEDIUM - 1).sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .sharedElement(
                                        state = rememberSharedContentState(key = "title/${item?.newsId}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    )
                            )

                            Text(
                                text = item?.description ?: "",
                                style = TextStyle(
                                    fontSize = FontSize.SMALL.sp,
                                    color = Gray,
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                            )
                        }

                        val imageRequest = ImageRequest.Builder(context)
                            .data(getUrlOfImageNotVideo(item?.urlList ?: emptyList()))
                            .crossfade(true)
                            .build()

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "News Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(90.dp)
                                .heightIn(max = 100.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                                .sharedElement(
                                    state = rememberSharedContentState(key = "image/${item?.newsId}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    renderInOverlayDuringTransition = true,
                                    clipInOverlayDuringTransition = OverlayClip(
                                        RoundedCornerShape(
                                            10.dp
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
        )
    }

}