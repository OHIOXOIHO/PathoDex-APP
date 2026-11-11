package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.model.HostGroup
import com.example.data.model.TransmissionRoute
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.Pd
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import com.example.ui.theme.TaxonomyIcons
import com.example.ui.theme.hostRoleColor
import com.example.ui.theme.transmissionLevelColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DossierSheet(
    disease: Disease,
    allDiseases: List<Disease>,
    isCompared: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLearned: () -> Unit,
    onToggleCompare: () -> Unit,
    onNoteChange: (String) -> Unit,
    onOpenLightbox: (String) -> Unit,
    onOpenRelated: (Disease) -> Unit,
    onSpeak: (String) -> Unit,
    onChipClick: (kind: String, slug: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    var fontScale by remember { mutableFloatStateOf(14.5f) }

    // یادداشت: متن سامانه پایه است؛ ویرایش کاربر فقط پس از تأیید بازنویسی می‌شود.
    val originalNote = disease.personal_note ?: ""
    var noteText by remember(disease.id) { mutableStateOf(originalNote) }
    var showOverwriteConfirm by remember { mutableStateOf(false) }
    val noteChanged = noteText != originalNote
    val images = disease.images ?: emptyList()

    // پیمایش سریع بخش‌های پرونده: هر بخش یک BringIntoViewRequester دارد تا
    // اسکرول به‌صورت بومی و بدون محاسبه‌ی دستی مختصات کار کند.
    // کلید remember علاوه بر شناسه، به فهرست بخش‌ها هم وابسته است چون پرونده
    // ابتدا بدون بخش‌های بالینی و سپس با بخش‌های کامل می‌آید.
    val sectionRequesters = remember(disease.id, disease.populatedSections) {
        disease.populatedSections.associate { it.key to BringIntoViewRequester() }
    }

    fun jumpToSection(key: String) {
        val requester = sectionRequesters[key] ?: return
        coroutineScope.launch { requester.bringIntoView() }
    }

    // Smooth Touch-Swipeable HorizontalPager with touch buttons and thumbnail selector
    val pagerState = rememberPagerState(pageCount = { if (images.isNotEmpty()) images.size else 1 })

    // Find related pathogens — همان منطق تاکسونومی سامانه‌ی وب
    val related = remember(disease.id, allDiseases) {
        disease.relatedPathogens(allDiseases)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("dossier_modal")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. هدر و نوار دسترسی سریع به بخش‌ها — همیشه روی پرونده می‌مانند و با
            //    پیمایش محتوا جابه‌جا نمی‌شوند. موقعیتشان به بخش یا آیتم انتخاب‌شده
            //    وابسته نیست و از لبه‌های پنجره و نوار سیستم جا باز می‌کنند.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PdSpacing.lg)
            ) {
                // 1. Header Bar: Category Badge + Disease ID + Close Button
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    Surface(
                        shape = PdShapes.sm,
                        color = CrimsonPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = disease.category,
                            fontSize = PdType.sp11_5,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs)
                        )
                    }

                    Text(
                        text = "شناسه پرونده: #${disease.id}",
                        fontSize = PdType.sp12,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = onDismiss,
                    shape = PdShapes.md,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.size(36.dp).testTag("close_dossier_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                }

                // نوار دسترسی سریع به بخش‌ها — چسبان، همیشه در دید، مستقل از انتخاب.
                // موقعیت این نوار ثابت است و با پیمایش محتوای پرونده تغییر نمی‌کند.
                if (disease.populatedSections.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PdSpacing.md))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(PdSize.iconSm)
                        )
                        Text(
                            text = "دسترسی سریع به بخش‌ها:",
                            fontSize = PdType.sp12,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dossier_section_chips"),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        items(disease.populatedSections, key = { it.key }) { section ->
                            AssistChip(
                                onClick = { jumpToSection(section.key) },
                                label = {
                                    Text(
                                        text = section.name_fa?.takeIf { it.isNotBlank() }
                                            ?: section.name_en?.takeIf { it.isNotBlank() }
                                            ?: section.key,
                                        fontSize = PdType.sp11_5,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = sectionIcon(section.key),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }

            // 2. محتوای پیمایش‌پذیر پرونده
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = PdSpacing.lg)
            ) {
            Spacer(modifier = Modifier.height(PdSpacing.md))

            // 2. Touch-Swipeable Gallery Hero with Touch Prev/Next Arrows and Thumbnail Strip
            if (images.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(PdShapes.lg)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("dossier_photo_pager")
                    ) { page ->
                        val img = images[page]
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(img.resolvedUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = disease.name_fa,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onOpenLightbox(img.resolvedUrl) }
                        )
                    }

                    // Bottom Vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )

                    // Touch Prev Arrow (<)
                    if (pagerState.currentPage > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = PdSpacing.sm)
                                .size(36.dp)
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Touch Next Arrow (>)
                    if (pagerState.currentPage < images.size - 1) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = PdSpacing.sm)
                                .size(36.dp)
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Fullscreen Lightbox Button (Top End)
                    val activeImg = images.getOrNull(pagerState.currentPage)
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(PdSpacing.compact)
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable {
                                activeImg?.let { onOpenLightbox(it.resolvedUrl) }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "بزرگنمایی",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Image Counter & Type (Bottom)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = PdSpacing.compact),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        Surface(
                            shape = PdShapes.md,
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "عکس ${pagerState.currentPage + 1} از ${images.size}",
                                fontSize = PdType.sp11,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                // Thumbnails Quick Selector Strip
                if (images.size > 1) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        itemsIndexed(images) { index, img ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(PdShapes.sm)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CrimsonPrimary else Color.Transparent,
                                        shape = PdShapes.sm
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(img.resolvedUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.cozy))

            // 3. Title & Scientific Nomenclature Card
            Card(
                shape = PdShapes.lg,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                    Text(
                        text = disease.name_fa,
                        fontSize = PdType.sp20,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!disease.name_en.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
                            modifier = Modifier.padding(top = PdSpacing.xs)
                        ) {
                            PdLtrText(
                                text = disease.name_en,
                                fontSize = PdType.sp13_5,
                                fontStyle = FontStyle.Italic,
                                color = CrimsonPrimary,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            FilledTonalIconButton(
                                onClick = { onSpeak(disease.name_en) },
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "تلفظ نام بیماری",
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                    // Pathogen Agent Box
                    Surface(
                        shape = PdShapes.md,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(PdSpacing.compact),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "عامل بیماری‌زا: " + disease.agent_fa,
                                    fontSize = PdType.sp13,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!disease.agent_en.isNullOrBlank()) {
                                    PdLtrText(
                                        text = disease.agent_en,
                                        fontSize = PdType.sp11_5,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!disease.agent_en.isNullOrBlank()) {
                                FilledTonalIconButton(
                                    onClick = { onSpeak(disease.agent_en) },
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "تلفظ نام عامل",
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Taxonomy family & class (Matched in size and height)
                    if (!disease.family_fa.isNullOrBlank() || !disease.class_fa.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(PdSpacing.sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                        ) {
                            Surface(
                                shape = PdShapes.md,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                            ) {
                                Column(modifier = Modifier.padding(PdSpacing.compact)) {
                                    Text(text = "خانواده (Family)", fontSize = PdType.sp10, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = disease.family_fa?.takeIf { it.isNotBlank() } ?: "—",
                                        fontSize = PdType.sp12,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!disease.family_en.isNullOrBlank()) {
                              PdLtrText(
                                            text = disease.family_en,
                                            fontSize = PdType.sp10,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = PdShapes.md,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                            ) {
                                Column(modifier = Modifier.padding(PdSpacing.compact)) {
                                    Text(text = "رده (Class)", fontSize = PdType.sp10, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = disease.class_fa?.takeIf { it.isNotBlank() } ?: "—",
                                        fontSize = PdType.sp12,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!disease.class_en.isNullOrBlank()) {
                         PdLtrText(
                                            text = disease.class_en,
                                            fontSize = PdType.sp10,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3b. Genus, Host Scope, Transmission Routes & Hosts (full dossier only)
            if (!disease.genus_fa.isNullOrBlank() || !disease.host_scope.isNullOrBlank() ||
                disease.transmission.isNotEmpty() || disease.hosts.isNotEmpty()
            ) {
                Card(
                    shape = PdShapes.lg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                        ) {
                            if (!disease.genus_fa.isNullOrBlank()) {
                                InfoCell(
                                    title = "سرده (Genus)",
                                    valueFa = disease.genus_fa,
                                    valueEn = disease.genus_en,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (!disease.host_scope.isNullOrBlank()) {
                                InfoCell(
                                    title = "محدوده میزبان",
                                    valueFa = disease.hostScopeLabel,
                                    valueEn = disease.host_scope,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // مسیرهای انتقال — چیپ‌های جمع‌شونده با آیکون و رنگ همان‌های وب
                        if (disease.transmission.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(PdSpacing.compact))
                            PdSectionHeader(
                                icon = Icons.Default.Coronavirus,
                                title = "مسیرهای انتقال",
                                trailing = {
                                    Text(
                                        text = "${disease.transmission.size} مسیر",
                                        fontSize = PdType.sp10_5,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.tight))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
                                verticalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                            ) {
                                disease.transmission.forEach { route ->
                                    TransmissionChip(
                                        route = route,
                                        onClick = { onChipClick("tx", route.slug) }
                                    )
                                }
                            }
                        }

                        // محدوده میزبان — چیپ‌های رنگی مثل وب (پاتوژن انسانی/غیرانسانی/هر دو)
                        if (!disease.host_scope.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(PdSpacing.compact))
                            PdSectionHeader(
                                icon = Icons.Default.HealthAndSafety,
                                title = "محدوده میزبان"
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.tight))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
                                verticalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                            ) {
                                HostScopeChip(scope = disease.host_scope)
                            }
                        }

                        // میزبان‌ها — چیپ‌های جمع‌شونده با آیکون، نام و نقش
                        if (disease.hosts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(PdSpacing.compact))
                            PdSectionHeader(
                                icon = Icons.Default.Pets,
                                title = "میزبان‌ها",
                                trailing = {
                                    Text(
                                        text = "${disease.hosts.size} میزبان",
                                        fontSize = PdType.sp10_5,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.tight))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
                                verticalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                            ) {
                                disease.hosts.forEach { host ->
                                    HostChip(
                                        host = host,
                                        onClick = { onChipClick("host", host.slug) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.cozy))
            }

            Spacer(modifier = Modifier.height(PdSpacing.cozy))

            // 4. Genuine Material 3 Action Buttons (No text splitting/wrapping!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                // Favorite Button
                FilledTonalButton(
                    onClick = onToggleFavorite,
                    shape = PdShapes.md,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (disease.is_favorite == true) CrimsonPrimary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(
                        imageVector = if (disease.is_favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (disease.is_favorite == true) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(PdSpacing.xs))
                    Text(
                        text = if (disease.is_favorite == true) "محبوب" else "علاقه‌مندی",
                        fontSize = PdType.sp11_5,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = if (disease.is_favorite == true) CrimsonPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Learned Button
                FilledTonalButton(
                    onClick = onToggleLearned,
                    shape = PdShapes.md,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (disease.is_learned == true) EmeraldLearned.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (disease.is_learned == true) EmeraldLearned else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(PdSpacing.xs))
                    Text(
                        text = if (disease.is_learned == true) "آموخته" else "یادگیری",
                        fontSize = PdType.sp11_5,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = if (disease.is_learned == true) EmeraldLearned else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Bookmark Button
                FilledTonalButton(
                    onClick = onToggleBookmark,
                    shape = PdShapes.md,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (disease.is_bookmarked == true) AmberBookmark.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(
                        imageVector = if (disease.is_bookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (disease.is_bookmarked == true) AmberBookmark else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(PdSpacing.xs))
                    Text(
                        text = if (disease.is_bookmarked == true) "نشان‌شده" else "نشان",
                        fontSize = PdType.sp11_5,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = if (disease.is_bookmarked == true) AmberBookmark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // Compare & Share Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                OutlinedButton(
                    onClick = onToggleCompare,
                    shape = PdShapes.md,
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = if (isCompared) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(PdSpacing.tight))
                    Text(
                        text = if (isCompared) "حذف از مقایسه" else "افزودن به مقایسه",
                        fontSize = PdType.sp11_5,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {
                        // اشتراک‌گذاری نشانی کانونیک همین پرونده روی سامانه‌ی وب،
                        // نه ساخت نشانی از روی نام نمایشی.
                        val url = disease.canonicalUrl
                        val title = disease.name_fa
                        val summary = disease.description_fa
                        val text = buildString {
                            append(title)
                            if (!disease.name_en.isNullOrBlank()) append(" (${disease.name_en})")
                            append("\n\n")
                            append(if (summary.length > 220) summary.take(220) + "…" else summary)
                            append("\n\n")
                            append(url)
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, title)
                            putExtra(Intent.EXTRA_TEXT, text)
                            // نسخه‌ی HTML باعث می‌شود نشانی در برنامه‌هایی که پشتیبانی
                            // می‌کنند به‌صورت پیوند قابل‌کلیک و قابل‌کپی دیده شود.
                            putExtra(
                                Intent.EXTRA_HTML_TEXT,
                                "<a href=\"$url\">$title</a>"
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "اشتراک‌گذاری پرونده‌ی پاتوژن")
                        )
                    },
                    shape = PdShapes.md,
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(PdSpacing.tight))
                    Text("اشتراک‌گذاری", fontSize = PdType.sp11_5, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.lg))

            // 5. Clinical Description (Monograph) with Font Resizer
            Card(
                shape = PdShapes.lg,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شرح بالینی و بیماری‌زایی",
                            fontSize = PdType.sp14,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Font Size Controls (A- / A+)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { if (fontScale > 11.5f) fontScale -= 1.5f }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("A-", fontSize = PdType.sp11, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(PdSpacing.tight))

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { if (fontScale < 21f) fontScale += 1.5f }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("A+", fontSize = PdType.sp11, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                    val clinicalPoints = remember(disease.description_fa) {
                        disease.description_fa
                            .split(Regex("(?<=[.؛\n])\\s+"))
                            .map { it.trim().removeSuffix(".").removeSuffix("؛").trim() }
                            .filter { it.isNotBlank() }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PdSpacing.xs)
                    ) {
                        clinicalPoints.forEach { point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = PdSpacing.sm)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonPrimary)
                                )
                                Text(
                                    text = point,
                                    fontSize = fontScale.sp,
                                    lineHeight = (fontScale * 1.55f).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                    // Copy description button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = PdShapes.sm,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clip(PdShapes.sm)
                                .clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Pathodex", disease.description_fa))
                                    Toast.makeText(context, "متن در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("کپی متن", fontSize = PdType.sp10_5)
                            }
                        }
                    }
                }
            }

            // 5b. Structured clinical sections from the full dossier (epidemiology, signs, ...)
            if (disease.populatedSections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(PdSpacing.cozy))

                Text(
                    text = "📚 بخش‌های پرونده",
                    fontSize = PdType.sp14,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(PdSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(PdSpacing.compact)) {
                    disease.populatedSections.forEach { section ->
                        val sectionRequester = sectionRequesters[section.key] ?: return@forEach
                        Card(
                            shape = PdShapes.md,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(sectionRequester)
                        ) {
                            Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                                Text(
                                    text = section.name_fa?.takeIf { it.isNotBlank() }
                                        ?: section.name_en?.takeIf { it.isNotBlank() }
                                        ?: section.key,
                                    fontSize = PdType.sp13,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                                Spacer(modifier = Modifier.height(PdSpacing.tight))
                                Text(
                                    text = section.body ?: "",
                                    fontSize = fontScale.sp,
                                    lineHeight = (fontScale * 1.55f).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.cozy))

            // 6. Personal Clinical Note — متن سامانه نمایش داده می‌شود و ویرایش با تأیید بازنویسی می‌شود
            Card(
                shape = PdShapes.lg,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(PdSpacing.cozy)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📝 یادداشت بالینی شخصی",
                                fontSize = PdType.sp13_5,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.xxs))
                            Text(
                                text = "نکات کلیدی، سوالات امتحانی یا پروتکل‌های درمانی این پاتوژن:",
                                fontSize = PdType.sp11,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (originalNote.isNotBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldLearned.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "همگام با سامانه",
                                    fontSize = PdType.sp9_5,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLearned,
                                    modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xxs)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("اینجا یادداشت خود را تایپ کنید...", fontSize = PdType.sp12) },
                        minLines = 3,
                        shape = PdShapes.md,
                        modifier = Modifier.fillMaxWidth().testTag("personal_note_input")
                    )

                    if (noteChanged) {
                        Spacer(modifier = Modifier.height(PdSpacing.compact))
                        Text(
                            text = "متن شما با یادداشت ذخیره‌شده در سامانه متفاوت است. ذخیره یعنی بازنویسی یادداشت سامانه.",
                            fontSize = PdType.sp10_5,
                            lineHeight = PdType.sp15,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PdSpacing.sm))
                        Button(
                            onClick = { showOverwriteConfirm = true },
                            shape = PdShapes.md,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("save_note_button")
                        ) {
                            Text(
                                text = if (originalNote.isBlank()) "ذخیره یادداشت" else "بازنویسی یادداشت سامانه",
                                fontSize = PdType.sp12,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // تأیید بازنویسی یادداشت سامانه
            if (showOverwriteConfirm) {
                AlertDialog(
                    onDismissRequest = { showOverwriteConfirm = false },
                    shape = PdShapes.lg,
                    title = {
                        Text(
                            text = "بازنویسی یادداشت سامانه",
                            fontSize = PdType.sp16,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "یادداشت ذخیره‌شده در سامانه‌ی وب با متن زیر بازنویسی می‌شود:",
                                fontSize = PdType.sp12_5,
                                lineHeight = PdType.sp18,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.compact))
                            Surface(
                                shape = PdShapes.md,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = noteText.ifBlank { "—" },
                                    fontSize = PdType.sp12,
                                    lineHeight = PdType.sp18,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(PdSpacing.compact)
                                )
                            }
                            if (originalNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(PdSpacing.compact))
                                Text(
                                    text = "یادداشت فعلی سامانه: $originalNote",
                                    fontSize = PdType.sp11,
                                    lineHeight = PdType.sp16,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showOverwriteConfirm = false
                                onNoteChange(noteText)
                            },
                            shape = PdShapes.md,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                        ) {
                            Text("بازنویسی و ذخیره", fontSize = PdType.sp12, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOverwriteConfirm = false }) {
                            Text("انصراف", fontSize = PdType.sp12)
                        }
                    }
                )
            }

            // 7. Related Pathogens Section — نزدیک‌ترین بر اساس رابطه‌ی تاکسونومی
            if (related.isNotEmpty()) {
                Spacer(modifier = Modifier.height(PdSpacing.lg))
                PdSectionHeader(
                    icon = Icons.Default.AccountTree,
                    title = "پاتوژن‌های نزدیک تاکسونومی",
                    trailing = {
                        Text(
                            text = "${related.size} مورد",
                            fontSize = PdType.sp10_5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Spacer(modifier = Modifier.height(PdSpacing.sm))

                Column(verticalArrangement = Arrangement.spacedBy(PdSpacing.sm)) {
                    related.forEach { rel ->
                        val other = rel.disease
                        Surface(
                            shape = PdShapes.md,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PdShapes.md)
                                .clickable { onOpenRelated(other) }
                        ) {
                            Row(
                                modifier = Modifier.padding(PdSpacing.compact),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                                    ) {
                                        Text(
                                            text = other.name_fa,
                                            fontSize = PdType.sp13,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            shape = PdShapes.sm,
                                            color = CrimsonPrimary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = rel.label,
                                                fontSize = PdType.sp10,
                                                fontWeight = FontWeight.Bold,
                                                color = CrimsonPrimary,
                                                modifier = Modifier.padding(
                                                    horizontal = PdSpacing.tight,
                                                    vertical = PdSpacing.xxs
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = "عامل: " + other.agent_fa,
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!other.genus_fa.isNullOrBlank()) {
                                        // رشته‌ی  mixed فارسی + لاتین را در یک Text معمولی می‌گذاریم تا
                                        // الگوریتم bidi ترتیب درست را در پاراگراف راست‌به‌چپ بگذارد؛
                                        // جداکردن اجباری به LTR می‌تواند جای علامت را عوض کند.
                                        Text(
                                            text = "سرده: " + (other.genus_en ?: other.genus_fa),
                                            fontSize = PdType.sp10,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "مشاهده",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.sheetBottomInset))
        }
        }
    }
}

/** خانه‌ی کوچک برچسب‌دار برای سرده و محدوده‌ی میزبان. */
@Composable
private fun InfoCell(
    title: String,
    valueFa: String,
    valueEn: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PdShapes.md,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.heightIn(min = 64.dp)
    ) {
        Column(modifier = Modifier.padding(PdSpacing.compact)) {
            Text(text = title, fontSize = PdType.sp10, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = valueFa,
                fontSize = PdType.sp12,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!valueEn.isNullOrBlank()) {
                PdLtrText(
                    text = valueEn,
                    fontSize = PdType.sp10,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * چیپ مسیر انتقال — هم‌شکل با چیپ‌های سامانه‌ی وب:
 * آیکون fa-* همان مسیر + نام فارسی + برچسب سطح، با رنگ سطح آن مسیر.
 */
@Composable
private fun TransmissionChip(
    route: TransmissionRoute,
    onClick: () -> Unit
) {
    val color = transmissionLevelColor(route.level)
    val name = route.name_fa?.takeIf { it.isNotBlank() }
        ?: route.name_en?.takeIf { it.isNotBlank() }
        ?: route.slug
    val level = route.level_fa?.takeIf { it.isNotBlank() }
        ?: when (route.level) {
            "primary" -> "اصلی"
            "secondary" -> "فرعی"
            "rare" -> "نادر"
            else -> null
        }

    Surface(
        onClick = onClick,
        shape = PdShapes.pill,
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
        ) {
            Icon(
                imageVector = TaxonomyIcons.transmission(route.icon ?: route.slug),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(PdSize.iconXs)
            )
            Text(
                text = name,
                fontSize = PdType.sp11,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            if (level != null) {
                Text(
                    text = level,
                    fontSize = PdType.sp10,
                    fontWeight = FontWeight.Black,
                    color = color.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * چیپ میزبان — آیکون fa-* همان گروه میزبان + نام فارسی + نام علمی (LTR) + نقش.
 */
@Composable
private fun HostChip(
    host: HostGroup,
    onClick: () -> Unit
) {
    val color = hostRoleColor(host.role)
    val name = host.name_fa?.takeIf { it.isNotBlank() }
        ?: host.name_en?.takeIf { it.isNotBlank() }
        ?: host.slug
    val role = host.role_fa?.takeIf { it.isNotBlank() } ?: host.role

    Surface(
        onClick = onClick,
        shape = PdShapes.pill,
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
        ) {
            Icon(
                imageVector = TaxonomyIcons.host(host.icon ?: host.slug),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(PdSize.iconXs)
            )
            Text(
                text = name,
                fontSize = PdType.sp11,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            if (!host.species_note.isNullOrBlank()) {
 PdLtrText(
                    text = host.species_note,
                    fontSize = PdType.sp10,
                    fontWeight = FontWeight.SemiBold,
                    color = color.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
            if (!role.isNullOrBlank()) {
                Text(
                    text = role,
                    fontSize = PdType.sp10,
                    fontWeight = FontWeight.Black,
                    color = color.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * چیپ محدوده میزبان — همان برچسب‌ها و رنگ‌های SCOPE_META سامانه‌ی وب.
 */
@Composable
private fun HostScopeChip(scope: String) {
    val (label, color) = when (scope) {
        "human" -> "پاتوژن انسانی" to MaterialTheme.colorScheme.error
        "non_human" -> "پاتوژن غیرانسانی" to MaterialTheme.colorScheme.tertiary
        "both" -> "انسانی و غیرانسانی" to Pd.warning
        else -> return
    }

    Surface(
        shape = PdShapes.pill,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(PdSize.iconXs)
            )
            Text(
                text = label,
                fontSize = PdType.sp11,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

/** آیکون هر بخش بالینی — هم‌نمای آیکون‌های fa-* که سامانه‌ی وب استفاده می‌کند. */
private fun sectionIcon(key: String): ImageVector = when (key) {
    "epidemiology" -> Icons.Default.Public
    "signs" -> Icons.Default.MonitorHeart
    "pathogenesis" -> Icons.Default.Coronavirus
    "diagnosis" -> Icons.Default.Science
    "treatment" -> Icons.Default.Medication
    "prevention" -> Icons.Default.HealthAndSafety
    "prognosis" -> Icons.Default.WarningAmber
    else -> Icons.Default.Notes
}
