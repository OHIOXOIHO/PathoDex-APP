package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.TaxonomyRefs
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdMotion
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import com.example.ui.viewmodel.InteractionFilter
import com.example.ui.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedSort: SortOrder,
    interactionFilter: InteractionFilter,
    hideLearned: Boolean,
    onSortChange: (SortOrder) -> Unit,
    onInteractionChange: (InteractionFilter) -> Unit,
    onToggleHideLearned: () -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit,
    taxonomyRefs: TaxonomyRefs? = null,
    txFilter: String? = null,
    hostFilter: String? = null,
    scopeFilter: String? = null,
    onTxFilterChange: (String?) -> Unit = {},
    onHostFilterChange: (String?) -> Unit = {},
    onScopeFilterChange: (String?) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PdSpacing.xl)
                // نوار سیستم پایین را حساب می‌کنیم تا دکمه‌های پایانی زیر آن پنهان نشوند
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = PdSpacing.xl)
                // وقتی فیلترهای تاکسونومی اضافه/حذف می‌شوند، نرم باز و بسته شود
                .animateContentSize(tween(PdMotion.normal))
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(PdSize.iconMd)
                    )
                    Text(
                        text = "فیلتر و مرتب‌سازی",
                        fontSize = PdType.sp16,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // هدف لمسی ۴۸dp حفظ می‌شود؛ فقط اندازه‌ی آیکون از مختصن می‌آید
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "بستن",
                        modifier = Modifier.size(PdSize.iconSm)
                    )
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.lg))

            // Sort Order Section
            Text(
                text = "مرتب‌سازی بر اساس",
                fontSize = PdType.sp13,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // Clean single-line sort chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                PdFilterChip(
                    selected = selectedSort == SortOrder.DATE_DESC,
                    onClick = { onSortChange(SortOrder.DATE_DESC) },
                    label = { Text("جدیدترین", fontSize = PdType.sp11_5, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )

                PdFilterChip(
                    selected = selectedSort == SortOrder.NAME_ASC,
                    onClick = { onSortChange(SortOrder.NAME_ASC) },
                    label = { Text("الفبای نام", fontSize = PdType.sp11_5, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )

                PdFilterChip(
                    selected = selectedSort == SortOrder.AGENT_ASC,
                    onClick = { onSortChange(SortOrder.AGENT_ASC) },
                    label = { Text("الفبای عامل", fontSize = PdType.sp11_5, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(PdSpacing.roomy))

            // User Interaction Filter Section (Spacious 2x2 layout, no wrapped texts)
            Text(
                text = "وضعیت مطالعه و نشان‌ها",
                fontSize = PdType.sp13,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // Row 1: All & Favorites
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                PdFilterChip(
                    selected = interactionFilter == InteractionFilter.ALL,
                    onClick = { onInteractionChange(InteractionFilter.ALL) },
                    label = { Text("همه پاتوژن‌ها", fontSize = PdType.sp11_5, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )

                PdFilterChip(
                    selected = interactionFilter == InteractionFilter.FAVORITES,
                    onClick = { onInteractionChange(InteractionFilter.FAVORITES) },
                    label = { Text("علاقه‌مندی‌ها", fontSize = PdType.sp11_5, maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(PdSize.iconXs)
                        )
                    },
                    accent = CrimsonPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // Row 2: Bookmarks & Learned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                PdFilterChip(
                    selected = interactionFilter == InteractionFilter.BOOKMARKS,
                    onClick = { onInteractionChange(InteractionFilter.BOOKMARKS) },
                    label = { Text("نشان‌شده‌ها", fontSize = PdType.sp11_5, maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(PdSize.iconXs)
                        )
                    },
                    accent = AmberBookmark,
                    modifier = Modifier.weight(1f)
                )

                PdFilterChip(
                    selected = interactionFilter == InteractionFilter.LEARNED,
                    onClick = { onInteractionChange(InteractionFilter.LEARNED) },
                    label = { Text("آموخته‌شده", fontSize = PdType.sp11_5, maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(PdSize.iconXs)
                        )
                    },
                    accent = EmeraldLearned,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(PdSpacing.roomy))

            // Hide Learned Switch Row
            Surface(
                shape = PdShapes.md,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PdSpacing.cozy, vertical = PdSpacing.compact),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مخفی کردن موارد آموخته‌شده",
                            fontSize = PdType.sp12_5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تمرکز مطالعه روی موارد مرور نشده",
                            fontSize = PdType.sp11,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.size(PdSpacing.sm))

                    PdSwitch(
                        checked = hideLearned,
                        onCheckedChange = { onToggleHideLearned() }
                    )
                }
            }

            // Taxonomy Filters (transmission routes / host groups / host scope) — mirrors the website
            val refs = taxonomyRefs
            if (refs != null && (refs.transmission_routes.isNotEmpty() || refs.host_groups.isNotEmpty())) {
                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                Text(
                    text = "راه انتقال و میزبان",
                    fontSize = PdType.sp13,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                if (refs.transmission_routes.isNotEmpty()) {
                    Text(
                        text = "مسیر انتقال",
                        fontSize = PdType.sp11_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.tight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        PdFilterChip(
                            selected = txFilter == null,
                            onClick = { onTxFilterChange(null) },
                            label = { Text("همه", fontSize = PdType.sp11, maxLines = 1) }
                        )
                        refs.transmission_routes.forEach { route ->
                            PdFilterChip(
                                selected = txFilter == route.slug,
                                onClick = { onTxFilterChange(route.slug) },
                                label = {
                                    val label = route.name_fa?.takeIf { it.isNotBlank() }
                                        ?: route.name_en?.takeIf { it.isNotBlank() }
                                        ?: route.slug
                                    Text(
                                        text = if (route.count > 0) "$label (${route.count})" else label,
                                        fontSize = PdType.sp11,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(PdSpacing.compact))
                }

                if (refs.host_groups.isNotEmpty()) {
                    Text(
                        text = "میزبان",
                        fontSize = PdType.sp11_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.tight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        PdFilterChip(
                            selected = hostFilter == null,
                            onClick = { onHostFilterChange(null) },
                            label = { Text("همه", fontSize = PdType.sp11, maxLines = 1) }
                        )
                        refs.host_groups.forEach { host ->
                            PdFilterChip(
                                selected = hostFilter == host.slug,
                                onClick = { onHostFilterChange(host.slug) },
                                label = {
                                    val label = host.name_fa?.takeIf { it.isNotBlank() }
                                        ?: host.name_en?.takeIf { it.isNotBlank() }
                                        ?: host.slug
                                    Text(
                                        text = if (host.count > 0) "$label (${host.count})" else label,
                                        fontSize = PdType.sp11,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(PdSpacing.compact))
                }

                // محدوده میزبان (انسانی / غیرانسانی / هر دو)
                val scopeCounts = refs.coverage?.host_scope
                val scopes = listOfNotNull(
                    ("human" to "انسانی").takeIf { (scopeCounts?.human ?: 0) > 0 },
                    ("non_human" to "غیرانسانی").takeIf { (scopeCounts?.non_human ?: 0) > 0 },
                    ("both" to "هر دو").takeIf { (scopeCounts?.both ?: 0) > 0 }
                )
                if (scopes.isNotEmpty()) {
                    Text(
                        text = "محدوده میزبان",
                        fontSize = PdType.sp11_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.tight))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        scopes.forEach { (slug, label) ->
                            PdFilterChip(
                                selected = scopeFilter == slug,
                                onClick = { onScopeFilterChange(slug) },
                                label = { Text(label, fontSize = PdType.sp11, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.xl))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
            ) {
                OutlinedButton(
                    onClick = { onResetFilters(); onDismiss() },
                    modifier = Modifier.weight(1f).height(PdSize.buttonHeight)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(PdSize.iconSm)
                    )
                    Text(
                        "پاکسازی",
                        fontSize = PdType.sp12,
                        modifier = Modifier.padding(start = PdSpacing.tight)
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    modifier = Modifier.weight(1f).height(PdSize.buttonHeight)
                ) {
                    Text(
                        "تایید",
                        fontSize = PdType.sp12_5,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
