package com.example.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Disease
import com.example.ui.components.PdEmptyState
import com.example.ui.components.PdIconAction
import com.example.ui.components.PdLoadingState
import com.example.ui.components.PathogenCard
import com.example.ui.components.PathogenGridCard
import com.example.ui.components.PathogenListItem
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    diseases: List<Disease>,
    allDiseases: List<Disease>,
    searchQuery: String,
    selectedCategory: String,
    isCompactListMode: Boolean,
    globalBlur: Boolean,
    compareIds: List<Int>,
    hasActiveFilters: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onToggleDensityMode: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onCardClick: (Disease) -> Unit,
    onToggleFavorite: (Disease) -> Unit,
    onToggleBookmark: (Disease) -> Unit,
    onToggleLearned: (Disease) -> Unit,
    onToggleCompare: (Disease) -> Unit,
    onSpeak: (String) -> Unit,
    onRandomDossier: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onRefreshData: () -> Unit = {}
) {
    // Dynamic categories list with item counts directly derived from server/local data
    val categories = remember(allDiseases) {
        val distinct = allDiseases.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        listOf("all" to allDiseases.size) + distinct.map { cat ->
            cat to allDiseases.count { it.category.trim() == cat }
        }
    }

    // Study progress calculation
    val learnedCount = remember(allDiseases) { allDiseases.count { it.is_learned == true } }
    val progressPercent = if (allDiseases.isNotEmpty()) (learnedCount * 100) / allDiseases.size else 0

    Box(modifier = modifier.fillMaxSize().testTag("explorer_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PdSpacing.lg)
        ) {
            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // 1. نوار جستجو + دکمه‌های چگالی و فیلتر
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "جستجو در نام، عامل یا علائم پاتوژن...",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(PdSize.iconMd)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(PdSize.iconSm))
                            }
                        }
                    },
                    // تک‌خطی و با ارتفاع ثابت: متن یا راهنمای بلند نباید فیلد را
                    // بزرگ کند و نباید عرض آن را جابه‌جا کند.
                    singleLine = true,
                    shape = PdShapes.md,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(PdSize.searchFieldHeight)
                        .testTag("search_input")
                )

                // Layout Mode Switcher (Compact List vs Cards)
                PdIconAction(
                    icon = if (isCompactListMode) Icons.Default.ViewAgenda else Icons.Default.GridView,
                    contentDescription = "تغییر حالت نمایش",
                    onClick = onToggleDensityMode,
                    tint = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("density_toggle_button")
                )

                // Filter Sheet Trigger
                PdIconAction(
                    icon = Icons.Default.Tune,
                    contentDescription = "فیلترها",
                    onClick = onOpenFilterSheet,
                    tint = if (hasActiveFilters) CrimsonPrimary else MaterialTheme.colorScheme.onSurface,
                    containerColor = if (hasActiveFilters) CrimsonPrimary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("open_filter_sheet_button")
                )
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // 2. چیپ‌های دسته‌بندی
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                items(categories) { (cat, count) ->
                    val isSelected = selectedCategory == cat

                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                            ) {
                                Text(
                                    text = if (cat == "all") "همه دسته‌ها" else cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Surface(
                                    shape = PdShapes.sm,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xxs)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = CrimsonPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = PdShapes.md,
                        modifier = Modifier.testTag("cat_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // 3. نوار وضعیت: تعداد و پیشرفت یادگیری
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نمایش ${diseases.size} پاتوژن از ${allDiseases.size}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    Text(
                        text = "پیشرفت یادگیری: $progressPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLearned
                    )
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .width(55.dp)
                            .height(5.dp)
                            .clip(PdShapes.pill),
                        color = EmeraldLearned,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // 4. محتوای اصلی
            when {
                allDiseases.isEmpty() && isLoading -> PdLoadingState(
                    message = "در حال دریافت فهرست پاتوژن‌ها...",
                    modifier = Modifier.fillMaxSize()
                )

                allDiseases.isEmpty() -> PdEmptyState(
                    icon = Icons.Default.CloudOff,
                    title = "اطلاعاتی در دستگاه ذخیره نیست",
                    message = "برای اولین دریافت فهرست پاتوژن‌ها از سامانه pathodex.ir، لطفاً اتصال اینترنت را برقرار نمایید.",
                    actionLabel = "تلاش دوباره",
                    onAction = onRefreshData,
                    modifier = Modifier.fillMaxSize()
                )

                diseases.isEmpty() -> PdEmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "موردی با این مشخصات یافت نشد",
                    message = "می‌توانید عبارت جستجو یا دسته‌بندی را تغییر دهید.",
                    actionLabel = "پاکسازی فیلترها",
                    onAction = {
                        onSearchChange("")
                        onCategoryChange("all")
                    },
                    modifier = Modifier.fillMaxSize()
                )

                isCompactListMode -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 85.dp),
                        verticalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(diseases, key = { it.id }) { item ->
                            PathogenListItem(
                                disease = item,
                                globalBlur = globalBlur,
                                isCompared = compareIds.contains(item.id),
                                onCardClick = { onCardClick(item) },
                                onToggleFavorite = { onToggleFavorite(item) },
                                onToggleBookmark = { onToggleBookmark(item) },
                                onToggleLearned = { onToggleLearned(item) },
                                onToggleCompare = { onToggleCompare(item) },
                                onSpeak = onSpeak
                            )
                        }
                    }
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 85.dp),
                    verticalArrangement = Arrangement.spacedBy(PdSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(diseases, key = { it.id }) { item ->
                        PathogenGridCard(
                            disease = item,
                            globalBlur = globalBlur,
                            isCompared = compareIds.contains(item.id),
                            onCardClick = { onCardClick(item) },
                            onToggleFavorite = { onToggleFavorite(item) },
                            onToggleBookmark = { onToggleBookmark(item) },
                            onToggleLearned = { onToggleLearned(item) },
                            onToggleCompare = { onToggleCompare(item) },
                            onSpeak = onSpeak
                        )
                    }
                }
            }
        }

        // دکمه‌ی شناور پرونده‌ی تصادفی
        ExtendedFloatingActionButton(
            onClick = onRandomDossier,
            containerColor = CrimsonPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = PdShapes.xl,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = PdSpacing.lg, bottom = PdSpacing.lg)
                .testTag("random_pathogen_fab")
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(PdSize.iconMd))
            Spacer(modifier = Modifier.width(PdSpacing.sm))
            Text("پرونده تصادفی", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
