package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Disease
import com.example.data.model.TaxCategoryNode
import com.example.data.model.TaxClassNode
import com.example.data.model.TaxEntry
import com.example.data.model.TaxFamilyNode
import com.example.data.model.buildTaxonomyTree
import com.example.data.model.flatten
import com.example.data.model.normalizeTaxonomyKey
import com.example.ui.components.PdEmptyState
import com.example.ui.components.PdLtrText
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdMotion
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType

/**
 * کاوشگر پوای تاکسونومی.
 *
 * ساختار دقیقاً مثل صفحه‌ی تاکسونومی سامانه‌ی وب: دسته › رده › خانواده › پاتوژن،
 * با شمارش تعداد در هر سطح، باز/بسته‌شدن شاخه‌ها و جستجو.
 *
 * درخت از روی داده‌ی واقعی ساخته می‌شود ([buildTaxonomyTree]) و هیچ فهرست ثابتی
 * ندارد. ردیف‌ها در یک LazyColumn صاف شده‌اند تا فقط گره‌های باز ترسیم شوند و
 * پیمایش با صدها پاتوژن نرم بماند. منطق گروه‌بندی، مرتب‌سازی و جستجو بدون
 * وابستگی به Compose است و در آزمون واحد پوشش داده شده است.
 */
@Composable
fun TaxonomyScreen(
    allDiseases: List<Disease>,
    onOpenDossier: (Disease) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = remember(query) { normalizeTaxonomyKey(query) }

    // ساختار درخت: دسته > رده > خانواده > پاتوژن‌ها
    val tree = remember(allDiseases, normalizedQuery) {
        buildTaxonomyTree(allDiseases, normalizedQuery)
    }

    // باز/بسته‌بودن شاخه‌ها. با هر بار تغییر درخت (یعنی تغییر جستجو) وضعیت باز باز
    // می‌شود تا نتیجه‌ها دیده شوند؛ بدون جستجو فقط دسته‌ها باز‌اند، مثل وب.
    val expanded = remember(tree, normalizedQuery) {
        mutableStateMapOf<String, Boolean>().apply {
            if (normalizedQuery.isBlank()) {
                tree.forEach { put(it.key, true) }
            } else {
                tree.forEach { cat ->
                    put(cat.key, true)
                    cat.classes.forEach { cls ->
                        put(cls.key, true)
                        cls.families.forEach { fam -> put(fam.key, true) }
                    }
                }
            }
        }
    }

    val rows = flatten(tree, expanded)

    val totalCategories = tree.size
    val totalClasses = tree.sumOf { it.classes.size }
    val totalFamilies = tree.sumOf { c -> c.classes.sumOf { it.families.size } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("taxonomy_screen")
    ) {
        // ── هدر و جستجو (چسبان) ────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = PdSpacing.lg)) {
            Spacer(modifier = Modifier.height(PdSpacing.sm))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(PdShapes.md)
                        .background(CrimsonPrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(PdSize.iconMd)
                    )
                }
                Column {
                    Text(
                        text = "درخت تاکسونومی",
                        fontSize = PdType.sp17,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ساختار سلسله‌مراتبی دسته، رده و خانواده عوامل بیماری‌زا",
                        fontSize = PdType.sp11,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(PdSpacing.md))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "جستجو در دسته، رده، خانواده، سرده یا نام پاتوژن...",
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
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "پاک کردن",
                                modifier = Modifier.size(PdSize.iconSm)
                            )
                        }
                    }
                },
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
                    .fillMaxWidth()
                    .height(PdSize.searchFieldHeight)
                    .testTag("taxonomy_search_input")
            )

            Spacer(modifier = Modifier.height(PdSpacing.sm))

            // ── آمار کلی ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                StatPill("دسته‌ها", totalCategories, Modifier.weight(1f))
                StatPill("رده‌ها", totalClasses, Modifier.weight(1f))
                StatPill("خانواده‌ها", totalFamilies, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(PdSpacing.sm))
        }

        // ── درخت ───────────────────────────────────────────────────────────
        if (rows.isEmpty()) {
            PdEmptyState(
                icon = Icons.Default.SearchOff,
                title = "موردی مطابق با عبارت جستجو یافت نشد",
                message = "عبارت دیگری را امتحان کنید یا عبارت جستجو را پاک کنید.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = PdSpacing.lg,
                    end = PdSpacing.lg,
                    bottom = PdSpacing.lg
                )
            ) {
                items(rows, key = { it.stableKey }) { row ->
                    when (row) {
                        is TaxEntry.Category -> CategoryNodeRow(
                            node = row.node,
                            isExpanded = expanded[row.nodeKey] == true,
                            onToggle = {
                                expanded[row.nodeKey] = !(expanded[row.nodeKey] ?: false)
                            }
                        )

                        is TaxEntry.Class -> ClassNodeRow(
                            node = row.node,
                            isExpanded = expanded[row.nodeKey] == true,
                            onToggle = {
                                expanded[row.nodeKey] = !(expanded[row.nodeKey] ?: false)
                            }
                        )

                        is TaxEntry.Family -> FamilyNodeRow(
                            node = row.node,
                            isExpanded = expanded[row.nodeKey] == true,
                            onToggle = {
                                expanded[row.nodeKey] = !(expanded[row.nodeKey] ?: false)
                            }
                        )

                        is TaxEntry.Pathogen -> DiseaseRow(
                            disease = row.disease,
                            onClick = { onOpenDossier(row.disease) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// اجزای ردیف
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatPill(label: String, count: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = PdShapes.md,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = PdSpacing.sm, horizontal = PdSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = PdType.sp14,
                fontWeight = FontWeight.Black,
                color = CrimsonPrimary
            )
            Text(
                text = label,
                fontSize = PdType.sp10,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryNodeRow(
    node: TaxCategoryNode,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = PdShapes.lg,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PdSpacing.xs)
            .animateContentSize(tween(PdMotion.normal))
            .clickable(onClick = onToggle)
            .testTag("tax_category_${node.key}")
    ) {
        Row(
            modifier = Modifier.padding(PdSpacing.cozy),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(PdSize.iconMd)
            )
            Text(
                text = node.title,
                fontSize = PdType.sp13_5,
                fontWeight = FontWeight.Black,
                color = if (isExpanded) CrimsonPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CountBadge(count = node.diseases.size, highlighted = isExpanded)
        }
    }
}

@Composable
private fun ClassNodeRow(
    node: TaxClassNode,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    NodeRow(
        title = "رده: ${node.titleFa}",
        titleEn = node.titleEn,
        count = node.diseases.size,
        isExpanded = isExpanded,
        highlighted = isExpanded,
        indent = PdSpacing.lg,
        tag = "tax_class_${node.key}",
        onClick = onToggle
    )
}

@Composable
private fun FamilyNodeRow(
    node: TaxFamilyNode,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    NodeRow(
        title = "خانواده: ${node.titleFa}",
        titleEn = node.titleEn,
        count = node.diseases.size,
        isExpanded = isExpanded,
        highlighted = isExpanded,
        indent = PdSpacing.xxl,
        tag = "tax_family_${node.key}",
        onClick = onToggle
    )
}

@Composable
private fun NodeRow(
    title: String,
    titleEn: String?,
    count: Int,
    isExpanded: Boolean,
    highlighted: Boolean,
    indent: Dp,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, top = PdSpacing.tight, bottom = PdSpacing.tight)
            .animateContentSize(tween(PdMotion.normal))
            .clickable(onClick = onClick)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(20.dp)
                .clip(PdShapes.xs)
                .background(
                    if (highlighted) CrimsonPrimary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
        )
        Spacer(modifier = Modifier.width(PdSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
            ) {
                Text(
                    text = title,
                    fontSize = PdType.sp11_5,
                    fontWeight = FontWeight.Bold,
                    color = if (highlighted) CrimsonPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(PdSize.iconSm)
                )
            }
            if (!titleEn.isNullOrBlank()) {
                PdLtrText(
                    text = titleEn,
                    fontSize = PdType.sp10,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(PdSpacing.sm))
        CountBadge(count = count, highlighted = highlighted)
    }
}

@Composable
private fun DiseaseRow(disease: Disease, onClick: () -> Unit) {
    Surface(
        shape = PdShapes.md,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = PdSpacing.huge, top = PdSpacing.xxs, bottom = PdSpacing.xxs)
            .clickable(onClick = onClick)
            .testTag("tax_disease_${disease.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disease.name_fa,
                    fontSize = PdType.sp12,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "عامل: " + disease.agent_fa,
                    fontSize = PdType.sp10_5,
                    color = CrimsonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!disease.genus_fa.isNullOrBlank()) {
                    // رشته‌ی mixed فارسی + لاتین در پاراگراف راست‌به‌چپ رهیده می‌شود
                    Text(
                        text = "سرده: " + (disease.genus_en ?: disease.genus_fa),
                        fontSize = PdType.sp10,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "#${disease.id}",
                fontSize = PdType.sp10,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CountBadge(count: Int, highlighted: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) CrimsonPrimary.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = count.toString(),
            fontSize = PdType.sp10_5,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xxs)
        )
    }
}
