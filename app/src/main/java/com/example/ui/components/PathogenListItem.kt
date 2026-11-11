package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Disease
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing

/**
 * مورد فهرست بالینی با چگالی بالا.
 *
 * ناحیه‌ی لمسی دکمه‌ها همیشه ٤٨dp است (در [PdIconAction] پیاده شده)؛ اندازه‌ی
 * ظاهری کمتر است تا چگالی فهرست حفظ شود. کل کارت هم یک ناحیه‌ی لمسی کامل است.
 */
@Composable
fun PathogenListItem(
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
    var localUnblur by remember { mutableStateOf(false) }
    val isImageBlurred = globalBlur && !localUnblur
    val firstImage = disease.images?.firstOrNull()

    PdCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(PdShapes.lg)
            .clickable { onCardClick() }
            .testTag("pathogen_list_item_${disease.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PdSpacing.md, vertical = PdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. تصویر یا آواتار دسته
            Box(
                modifier = Modifier
                    .size(PdSize.listThumb)
                    .clip(PdShapes.md)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (firstImage != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(firstImage.resolvedUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = disease.name_fa,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isImageBlurred) Modifier.blur(14.dp) else Modifier)
                            .clickable {
                                if (globalBlur) localUnblur = !localUnblur
                                else onCardClick()
                            }
                    )
                    if (isImageBlurred) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "نمایش",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(PdSize.iconMd)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Biotech,
                        contentDescription = disease.category,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(PdSpacing.md))

            // 2. نام، نام لاتین و عامل
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    Text(
                        text = disease.name_fa,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(shape = PdShapes.sm, color = CrimsonPrimary.copy(alpha = 0.12f)) {
                        Text(
                            text = disease.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(
                                horizontal = PdSpacing.sm,
                                vertical = PdSpacing.xxs
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.xxs))

                if (!disease.name_en.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                    ) {
                        PdLtrText(
                            text = disease.name_en,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // تلفظ نام لاتین — ناحیه‌ی لمسی ٤٨dp با سطح دیداری کوچک
                        PdIconAction(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "تلفظ",
                            onClick = { onSpeak(disease.name_en) },
                            tint = CrimsonPrimary,
                            containerColor = CrimsonPrimary.copy(alpha = 0.1f),
                            visualSize = 28.dp,
                            iconSize = PdSize.iconSm
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.xxs))

                Text(
                    text = "عامل: " + disease.agent_fa,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(PdSpacing.sm))

            // 3. اقدامات سریع — ناحیه‌ی لمسی هر کدام ٤٨dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
            ) {
                PdIconAction(
                    icon = if (disease.is_favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "علاقه‌مندی",
                    onClick = onToggleFavorite,
                    tint = if (disease.is_favorite == true) CrimsonPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = if (disease.is_favorite == true) CrimsonPrimary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )

                PdIconAction(
                    icon = Icons.Default.Check,
                    contentDescription = "آموخته‌شده",
                    onClick = onToggleLearned,
                    tint = if (disease.is_learned == true) EmeraldLearned
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // نوار نشان‌های وضعیت فقط وقتی حداقل یکی از نشان‌ها فعال است
        val badges = buildList {
            if (disease.is_bookmarked == true) add(AmberBookmark to "بوکمارک")
            if (isCompared) add(CrimsonPrimary to "مقایسه")
        }
        if (badges.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = PdSpacing.md, end = PdSpacing.md, bottom = PdSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                badges.forEach { (color, label) ->
                    Surface(shape = PdShapes.pill, color = color.copy(alpha = 0.14f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier = Modifier.padding(
                                horizontal = PdSpacing.sm,
                                vertical = PdSpacing.xxs
                            )
                        )
                    }
                }
            }
        }
    }
}
