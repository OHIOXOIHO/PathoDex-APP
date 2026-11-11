package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Disease
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import com.example.ui.components.PdLtrText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    deck: List<Disease>,
    currentIndex: Int,
    isFlipped: Boolean,
    selectedCategory: String,
    allDiseases: List<Disease>,
    onCategoryChange: (String) -> Unit,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onShuffle: () -> Unit,
    onToggleLearned: (Disease) -> Unit,
    onOpenDossier: (Disease) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("all") + allDiseases.map { it.category }.distinct().filter { it.isNotBlank() }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PdSpacing.lg)
            .testTag("flashcards_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(PdSpacing.compact))

        // Category Filter Chips & Shuffle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val label = if (cat == "all") "همه دسته‌ها" else cat

                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = {
                            Text(
                                text = label,
                                fontSize = PdType.sp12,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = CrimsonPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = PdShapes.md
                    )
                }
            }

            Spacer(modifier = Modifier.width(PdSpacing.sm))

            FilledTonalIconButton(
                onClick = onShuffle,
                shape = PdShapes.md,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("shuffle_flashcards_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "بُر زدن",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.md))

        // Progress Counter Pill
        if (deck.isNotEmpty()) {
            Surface(
                shape = PdShapes.md,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(bottom = PdSpacing.tight)
            ) {
                Text(
                    text = "کارت ${currentIndex + 1} از ${deck.size}",
                    fontSize = PdType.sp12,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = PdSpacing.md, vertical = PdSpacing.xs)
                )
            }
        }

        // 3D Flip Card Container
        if (deck.isEmpty()) {
            ElevatedCard(
                shape = PdShapes.xxl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ کارتی در این دسته‌بندی یافت نشد.",
                        fontSize = PdType.sp13_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val currentDisease = deck[currentIndex % deck.size]

            ElevatedCard(
                shape = PdShapes.xxl,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp, pressedElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(PdShapes.xxl)
                    .border(
                        width = 1.dp,
                        color = if (currentDisease.is_learned == true) EmeraldLearned.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = PdShapes.xxl
                    )
                    .clickable { onFlip() }
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 14f * density
                    }
                    .testTag("interactive_flashcard")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (rotation <= 90f) {
                        // FRONT FACE (Pre-composed for butter-smooth 60fps flip)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PdSpacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = PdShapes.md,
                                color = CrimsonPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentDisease.category,
                                    fontSize = PdType.sp11_5,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary,
                                    modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs)
                                )
                            }

                            if (currentDisease.is_learned == true) {
                                Surface(
                                    shape = PdShapes.md,
                                    color = EmeraldLearned.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "آموخته‌شده ✓",
                                        fontSize = PdType.sp10_5,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLearned,
                                        modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs)
                                    )
                                }
                            }
                        }

                        // Pathogen Hero Image
                        val firstImg = currentDisease.images?.firstOrNull()
                        if (firstImg != null) {
                            Box(
                                modifier = Modifier
                                    .size(135.dp)
                                    .clip(PdShapes.xl)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), PdShapes.xl)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(firstImg.resolvedUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = currentDisease.name_fa,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(110.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Biotech,
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentDisease.name_fa,
                                fontSize = PdType.sp22,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (!currentDisease.name_en.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs),
                                    modifier = Modifier.padding(top = PdSpacing.xs)
                                ) {
                                    PdLtrText(
                                        text = currentDisease.name_en,
                                        fontSize = PdType.sp14,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { onSpeak(currentDisease.name_en) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = CrimsonPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = PdShapes.md,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
                                modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.tight)
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "لمس برای چرخش سه‌بعدی و نمایش پاسخ",
                                    fontSize = PdType.sp11,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    } else {
                        // BACK FACE (Rotated 180 degrees)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = 180f
                                }
                                .padding(PdSpacing.xl),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentDisease.name_fa,
                                    fontSize = PdType.sp16_5,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(PdSpacing.sm))
                                Surface(
                                    shape = PdShapes.sm,
                                    color = CrimsonPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "مشخصات بالینی",
                                        fontSize = PdType.sp10_5,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(PdSpacing.compact))

                            // Agent, Family, Class Card
                            Surface(
                                shape = PdShapes.md,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(PdSpacing.md)) {
                                    Text(
                                        text = "عامل: ${currentDisease.agent_fa} ${if (!currentDisease.agent_en.isNullOrBlank()) "(${currentDisease.agent_en})" else ""}",
                                        fontSize = PdType.sp12_5,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )
                                    Text(
                                        text = "خانواده: ${currentDisease.family_fa ?: currentDisease.family_en ?: "—"}",
                                        fontSize = PdType.sp11_5,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                    Text(
                                        text = "رده: ${currentDisease.class_fa ?: currentDisease.class_en ?: "—"}",
                                        fontSize = PdType.sp11_5,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(PdSpacing.compact))

                            Text(
                                text = currentDisease.description_fa,
                                fontSize = PdType.sp12_5,
                                lineHeight = PdType.sp20,
                                textAlign = TextAlign.Justify,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Back Face Footer: Real Native Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { onOpenDossier(currentDisease) },
                                shape = PdShapes.md,
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text("مشاهده پرونده کامل", fontSize = PdType.sp12, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onToggleLearned(currentDisease) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentDisease.is_learned == true) EmeraldLearned else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = PdShapes.md,
                                modifier = Modifier.height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (currentDisease.is_learned == true) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (currentDisease.is_learned == true) "یاد گرفتم ✓" else "افزودن به آموخته‌ها",
                                        fontSize = PdType.sp12,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentDisease.is_learned == true) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // Native Navigation Control Buttons (Prev, Flip, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onPrev,
                shape = PdShapes.md,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("flashcard_prev_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "قبلی", modifier = Modifier.size(17.dp))
                    Text("قبلی", fontSize = PdType.sp12_5, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }

            Button(
                onClick = onFlip,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = PdShapes.md,
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("flashcard_flip_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)) {
                    Icon(Icons.Default.RotateRight, contentDescription = "چرخش", modifier = Modifier.size(18.dp))
                    Text("چرخش کارت", fontSize = PdType.sp12_5, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                }
            }

            FilledTonalButton(
                onClick = onNext,
                shape = PdShapes.md,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("flashcard_next_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                    Text("بعدی", fontSize = PdType.sp12_5, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بعدی", modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
