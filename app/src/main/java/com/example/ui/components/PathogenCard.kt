package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Disease
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import kotlinx.coroutines.launch

/**
 * 2-Column Grid Card: Perfectly uniform in height (236dp) so both columns align symmetrically.
 * Completely free of noisy text overlays on the medical photos.
 */
@Composable
fun PathogenGridCard(
    disease: Disease,
    globalBlur: Boolean,
    isCompared: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLearned: () -> Unit,
    onToggleCompare: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var localUnblur by remember { mutableStateOf(false) }
    val isImageBlurred = globalBlur && !localUnblur
    val images = disease.images ?: emptyList()
    val pagerState = rememberPagerState(pageCount = { if (images.isNotEmpty()) images.size else 1 })

    ElevatedCard(
        shape = PdShapes.lg,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(PdSize.gridCardHeight)
            .clip(PdShapes.lg)
            .border(
                width = if (disease.is_learned == true) 1.5.dp else 1.dp,
                color = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = PdShapes.lg
            )
            .clickable { onCardClick() }
            .testTag("pathogen_grid_card_${disease.id}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Fixed-Height Pristine Media Header (112dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PdSize.gridMediaHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (images.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val img = images[page]
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(img.resolvedUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = disease.name_fa,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (isImageBlurred) Modifier.blur(20.dp) else Modifier)
                                .clickable {
                                    if (globalBlur) {
                                        localUnblur = !localUnblur
                                    } else {
                                        onCardClick()
                                    }
                                }
                        )
                    }

                    // Subtle bottom gradient for smooth transition
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PdSize.gridGradientHeight)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                                )
                            )
                    )

                    // Touch Prev/Next Arrows for multiple images (minimalist, no text clutter)
                    if (images.size > 1) {
                        if (pagerState.currentPage > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = PdSpacing.xs)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "عکس قبل",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (pagerState.currentPage < images.size - 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = PdSpacing.xs)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "عکس بعد",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Minimal dots indicator (no text like "1 of 2")
                        Row(
                            modifier = Modifier
                                .padding(bottom = PdSpacing.tight)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                        ) {
                            repeat(images.size) { idx ->
                                val isSelected = pagerState.currentPage == idx
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(if (isSelected) 14.dp else 4.dp)
                                        .clip(PdShapes.xs)
                                        .background(if (isSelected) CrimsonPrimary else Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }

                    if (isImageBlurred) {
                        Surface(
                            shape = PdShapes.sm,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable { localUnblur = true }
                        ) {
                            Text(
                                text = "لمس برای مشاهده",
                                fontSize = PdType.sp10,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs)
                            )
                        }
                    }
                } else {
                    // Fallback clinical placeholder (matching exact 112dp height)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔬",
                            fontSize = PdType.sp28
                        )
                    }
                }
            }

            // Card Body (Balanced, fixed height)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PdSpacing.compact, vertical = PdSpacing.sm),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Category Tag
                    Text(
                        text = disease.category,
                        fontSize = PdType.sp10,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.xxs))

                    // Disease Name
                    Text(
                        text = disease.name_fa,
                        fontSize = PdType.sp13,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.xxs))

                    // Agent Name
                    Text(
                        text = disease.agent_fa,
                        fontSize = PdType.sp11,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom Action Buttons Row — ناحیه‌ی لمسی هر دکمه ٤٨dp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PdIconAction(
                        icon = if (disease.is_favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "علاقه‌مندی",
                        onClick = onToggleFavorite,
                        tint = if (disease.is_favorite == true) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = if (disease.is_favorite == true) CrimsonPrimary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        visualSize = 40.dp,
                        iconSize = PdSize.iconMd
                    )

                    PdIconAction(
                        icon = if (disease.is_bookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "نشان‌گذاری",
                        onClick = onToggleBookmark,
                        tint = if (disease.is_bookmarked == true) AmberBookmark else MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = if (disease.is_bookmarked == true) AmberBookmark.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        visualSize = 40.dp,
                        iconSize = PdSize.iconMd
                    )

                    PdIconAction(
                        icon = Icons.Default.Check,
                        contentDescription = "آموخته‌شده",
                        onClick = onToggleLearned,
                        tint = if (disease.is_learned == true) EmeraldLearned else MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        visualSize = 40.dp,
                        iconSize = PdSize.iconMd
                    )
                }
            }
        }
    }
}

/**
 * Full Detailed Pathogen Card (1-Column view or standalone).
 * Clean photos with NO noisy badge overlays.
 */
@Composable
fun PathogenCard(
    disease: Disease,
    globalBlur: Boolean,
    isCompared: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLearned: () -> Unit,
    onToggleCompare: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var localUnblur by remember { mutableStateOf(false) }
    val isImageBlurred = globalBlur && !localUnblur
    val images = disease.images ?: emptyList()
    val pagerState = rememberPagerState(pageCount = { if (images.isNotEmpty()) images.size else 1 })

    ElevatedCard(
        shape = PdShapes.xl,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(PdShapes.xl)
            .border(
                width = if (disease.is_learned == true) 1.5.dp else 1.dp,
                color = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = PdShapes.xl
            )
            .clickable { onCardClick() }
            .testTag("pathogen_card_${disease.id}")
    ) {
        Column {
            // Media Header: Clean photo with no text badge overlays
            if (images.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("photo_pager_${disease.id}")
                    ) { page ->
                        val img = images[page]
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(img.resolvedUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = disease.name_fa,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (isImageBlurred) Modifier.blur(20.dp) else Modifier)
                                .clickable {
                                    if (globalBlur) {
                                        localUnblur = !localUnblur
                                    } else {
                                        onCardClick()
                                    }
                                }
                        )
                    }

                    // Touch Prev/Next Arrows for image navigation
                    if (images.size > 1) {
                        if (pagerState.currentPage > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = PdSpacing.tight)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "عکس قبلی",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        if (pagerState.currentPage < images.size - 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = PdSpacing.tight)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "عکس بعدی",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Minimal swipe dots indicator (No text overlays)
                        Row(
                            modifier = Modifier
                                .padding(bottom = PdSpacing.sm)
                                .align(Alignment.BottomCenter),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                        ) {
                            repeat(images.size) { iteration ->
                                val isSelected = pagerState.currentPage == iteration
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(if (isSelected) 18.dp else 5.dp)
                                        .clip(PdShapes.xs)
                                        .background(if (isSelected) CrimsonPrimary else Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }

                    if (isImageBlurred) {
                        Button(
                            onClick = { localUnblur = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                            shape = PdShapes.md,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(PdSpacing.tight))
                            Text(
                                text = "لمس برای مشاهده تصویر",
                                fontSize = PdType.sp11,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔬 " + disease.category,
                        fontSize = PdType.sp14,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Card Body
            Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                // Category & Disease Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = disease.name_fa,
                        fontSize = PdType.sp16,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = PdShapes.sm,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = disease.category,
                            fontSize = PdType.sp10_5,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xxs)
                        )
                    }
                }

                if (!disease.name_en.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs),
                        modifier = Modifier.padding(top = PdSpacing.xxs)
                    ) {
                        PdLtrText(
                            text = disease.name_en,
                            fontSize = PdType.sp12,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        PdIconAction(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "تلفظ",
                            onClick = { onSpeak(disease.name_en) },
                            tint = CrimsonPrimary,
                            containerColor = CrimsonPrimary.copy(alpha = 0.12f),
                            visualSize = 28.dp,
                            iconSize = PdSize.iconXs
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                // Agent Info Row
                Surface(
                    shape = PdShapes.md,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.tight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "عامل: " + disease.agent_fa,
                            fontSize = PdType.sp12,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!disease.agent_en.isNullOrBlank()) {
                            PdIconAction(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "تلفظ عامل",
                                onClick = { onSpeak(disease.agent_en) },
                                tint = CrimsonPrimary,
                                containerColor = CrimsonPrimary.copy(alpha = 0.12f),
                                visualSize = 28.dp,
                                iconSize = PdSize.iconXs
                            )
                        }
                    }
                }

                // Description
                Text(
                    text = disease.description_fa,
                    fontSize = PdType.sp11_5,
                    lineHeight = PdType.sp18,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = PdSpacing.tight)
                )

                Spacer(modifier = Modifier.height(PdSpacing.compact))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                        PdIconAction(
                            icon = if (disease.is_favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "علاقه‌مندی",
                            onClick = onToggleFavorite,
                            tint = if (disease.is_favorite == true) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (disease.is_favorite == true) CrimsonPrimary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            visualSize = 40.dp,
                            iconSize = PdSize.iconMd
                        )

                        PdIconAction(
                            icon = if (disease.is_bookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "نشان‌گذاری",
                            onClick = onToggleBookmark,
                            tint = if (disease.is_bookmarked == true) AmberBookmark else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (disease.is_bookmarked == true) AmberBookmark.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            visualSize = 40.dp,
                            iconSize = PdSize.iconMd
                        )

                        PdIconAction(
                            icon = Icons.Default.Check,
                            contentDescription = "آموخته‌شده",
                            onClick = onToggleLearned,
                            tint = if (disease.is_learned == true) EmeraldLearned else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            visualSize = 40.dp,
                            iconSize = PdSize.iconMd
                        )

                        PdIconAction(
                            icon = Icons.Default.Scale,
                            contentDescription = "مقایسه",
                            onClick = onToggleCompare,
                            tint = if (isCompared) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (isCompared) CrimsonPrimary.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            visualSize = 40.dp,
                            iconSize = PdSize.iconMd
                        )
                    }

                    FilledTonalButton(
                        onClick = onCardClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CrimsonPrimary.copy(alpha = 0.12f),
                            contentColor = CrimsonPrimary
                        ),
                        shape = PdShapes.md,
                        modifier = Modifier.height(PdSize.buttonHeightSm)
                    ) {
                        Text("پرونده بالینی", fontSize = PdType.sp11, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
