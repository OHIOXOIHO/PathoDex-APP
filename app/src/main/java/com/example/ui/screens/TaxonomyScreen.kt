package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.model.Disease
import com.example.data.model.TaxCategoryNode
import com.example.data.model.TaxEntry
import com.example.data.model.buildTaxonomyTree
import com.example.data.model.flatten
import com.example.data.model.normalizeTaxonomyKey
import com.example.ui.components.PdEmptyState
import com.example.ui.components.PdLtrText
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.Pd
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("taxonomy_screen")
    ) {
        TaxonomyHeader(
            query = query,
            onQueryChange = { query = it },
            tree = tree,
            onExpandAll = {
                tree.forEach { cat ->
                    expanded[cat.key] = true
                    cat.classes.forEach { cls ->
                        expanded[cls.key] = true
                        cls.families.forEach { fam -> expanded[fam.key] = true }
                    }
                }
            },
            onCollapseAll = {
                tree.forEach { cat ->
                    expanded[cat.key] = false
                    cat.classes.forEach { cls ->
                        expanded[cls.key] = false
                        cls.families.forEach { fam -> expanded[fam.key] = false }
                    }
                }
            }
        )

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

                        is TaxEntry.Class -> TreeNodeRow(
                            depth = row.depth,
                            title = row.node.titleFa,
                            subtitle = row.node.titleEn,
                            count = row.node.diseases.size,
                            isExpanded = expanded[row.nodeKey] == true,
                            onToggle = {
                                expanded[row.nodeKey] = !(expanded[row.nodeKey] ?: false)
                            },
                            tag = "tax_class_${row.node.key}",
                            prefix = "رده: ",
                            icon = Icons.Default.Layers,
                            accent = Pd.palette.warning
                        )

                        is TaxEntry.Family -> TreeNodeRow(
                            depth = row.depth,
                            title = row.node.titleFa,
                            subtitle = row.node.titleEn,
                            count = row.node.diseases.size,
                            isExpanded = expanded[row.nodeKey] == true,
                            onToggle = {
                                expanded[row.nodeKey] = !(expanded[row.nodeKey] ?: false)
                            },
                            tag = "tax_family_${row.node.key}",
                            prefix = "خانواده: ",
                            icon = Icons.Default.AccountTree,
                            accent = Pd.palette.success
                        )

                        is TaxEntry.Pathogen -> PathogenNodeRow(
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
// هدر: عنوان، جستجو و خلاصه‌ی شمارش‌ها
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TaxonomyHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    tree: List<TaxCategoryNode>,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit
) {
    val totalCategories = tree.size
    val totalClasses = tree.sumOf { it.classes.size }
    val totalFamilies = tree.sumOf { c -> c.classes.sumOf { it.families.size } }
    val totalPathogens = tree.sumOf { it.diseases.size }

    Column(modifier = Modifier.padding(horizontal = PdSpacing.lg)) {
        Spacer(modifier = Modifier.height(PdSpacing.sm))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(PdSize.iconTile)
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "درخت تاکسونومی",
                    fontSize = PdType.sp16,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "دسته › رده › خانواده › پاتوژن",
                    fontSize = PdType.sp11,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // باز کردن/بستن همه‌ی شاخه‌ها — مثل درخت‌نمای سیستم‌عامل
            IconButton(onClick = onCollapseAll) {
                Icon(
                    imageVector = Icons.Default.UnfoldLess,
                    contentDescription = "بستن همه",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(PdSize.iconSm)
                )
            }
            IconButton(onClick = onExpandAll) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = "باز کردن همه",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(PdSize.iconSm)
                )
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.md))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
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
                    IconButton(onClick = { onQueryChange("") }) {
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

        // خلاصه‌ی یک‌خطی به‌جای سه کارت آماری — فضای کمتری می‌گیرد و یک‌جا خوانده می‌شود
        Text(
            text = "$totalPathogens پاتوژن · $totalCategories دسته · $totalClasses رده · $totalFamilies خانواده",
            fontSize = PdType.sp11,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(PdSpacing.sm))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ردیف‌های درخت
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ریل عمودی درخت.
 *
 * برای هر سطح والد یک خط عمودی کشیده می‌شود و یک اتصال افقی به نشانگر همین
 * گره می‌رسد، مثل درخت‌نمای سیستم‌عامل. در چیدمان راست‌به‌چپ ریل در سمت راست
 * قرار می‌گیرد و شاخه‌ها به سمت چپ تورفتگی می‌گیرند.
 */
@Composable
private fun TreeRail(depth: Int, color: Color) {
    Canvas(modifier = Modifier.width(PdSpacing.treeIndent * depth)) {
        val step = PdSpacing.treeIndent.toPx()
        val stroke = 1.dp.toPx()

        // خط عمودی هر سطح والد — از لبه‌ی start (راست در راست‌به‌چپ) شمرده می‌شود
        for (i in 0 until depth) {
            val x = size.width - (i * step + step / 2f)
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = stroke
            )
        }

        // اتصال افقی از ریل والد به نشانگر این گره
        val y = size.height / 2f
        val xParent = size.width - ((depth - 1) * step + step / 2f)
        drawLine(
            color = color,
            start = Offset(xParent, y),
            end = Offset(size.width, y),
            strokeWidth = stroke
        )
    }
}

/**
 * پیکان باز/بسته‌ی درخت — مثل صفحه‌ی تاکسونومی سامانه‌ی وب.
 *
 * بسته به جهت راست‌به‌چپ، پیکان به سمت چپ می‌نشیند (جهت باز شدن شاخه‌ها) و
 * وقتی شاخه باز است ۹- درجه می‌چرخد تا به سمت پایین نشانه بگیرد. در حالت باز
 * رنگ برند می‌گیرد، دقیقاً مثل `tree-chevron` در وب.
 */
@Composable
private fun TreeChevron(expanded: Boolean, tint: Color, modifier: Modifier = Modifier) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    // بسته به جهت: باز شده همیشه به سمت پایین؛ بسته در راست‌به‌چپ به سمت چپ
    // و در چپ‌به‌راست به سمت راست — مثل tree-chevron در سامانه‌ی وب.
    val rotation by animateFloatAsState(
        targetValue = when {
            expanded -> 90f
            rtl -> 180f
            else -> 0f
        },
        animationSpec = tween(PdMotion.normal),
        label = "treeChevron"
    )
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(PdSize.iconSm)
            .rotate(rotation)
    )
}

/** گره‌ی دسته — ریشه‌ی درخت، با کارت مشخص تا از شاخه‌ها جدا دیده شود. */
@Composable
private fun CategoryNodeRow(
    node: TaxCategoryNode,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val accent = CrimsonPrimary
    Surface(
        shape = PdShapes.lg,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PdSpacing.xs)
            .animateContentSize(tween(PdMotion.normal))
            .clickable(onClick = onToggle)
            .testTag("tax_category_${node.key}")
    ) {
        Row(
            modifier = Modifier.padding(PdSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            TreeChevron(
                expanded = isExpanded,
                tint = if (isExpanded) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = node.title,
                fontSize = PdType.sp14,
                fontWeight = FontWeight.Black,
                color = if (isExpanded) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CountBadge(
                count = node.diseases.size,
                highlighted = isExpanded,
                accent = accent
            )
        }
    }
}

/**
 * گره‌ی رده یا خانواده.
 *
 * هر سطح هم با پیشوند و هم با آیکون و رنگ خودش مشخص می‌شود — مثل وب:
 * رده با آیکون لایه‌ها و رنگ کهربایی، خانواده با آیکون درخت و رنگ سبز. بدون این
 * نشانه‌ها رده‌ها از دسته‌ها قابل‌تشخیص نیستند.
 */
@Composable
private fun TreeNodeRow(
    depth: Int,
    title: String,
    subtitle: String?,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    tag: String,
    prefix: String,
    icon: ImageVector,
    accent: Color
) {
    val railColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .animateContentSize(tween(PdMotion.normal))
            .clickable(onClick = onToggle)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TreeRail(depth = depth, color = railColor)

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = PdSpacing.tight, horizontal = PdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            TreeChevron(
                expanded = isExpanded,
                tint = if (isExpanded) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(PdSize.iconSm)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prefix + title,
                    fontSize = PdType.sp12_5,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpanded) accent
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    PdLtrText(
                        text = subtitle,
                        fontSize = PdType.sp10,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CountBadge(count = count, highlighted = isExpanded, accent = accent)
        }
    }
}

/** گره‌ی برگ — خود پاتوژن. */
@Composable
private fun PathogenNodeRow(disease: Disease, onClick: () -> Unit) {
    val railColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .testTag("tax_disease_${disease.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TreeRail(depth = 3, color = railColor)

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = PdSpacing.sm, horizontal = PdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            // نشانگر برگ — نقطه‌ی کوچک به‌جای پیکان باز/بسته
            Box(
                modifier = Modifier
                    .size(PdSize.treeDot)
                    .clip(CircleShape)
                    .background(CrimsonPrimary.copy(alpha = 0.65f))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disease.name_fa,
                    fontSize = PdType.sp12,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = disease.agent_fa,
                    fontSize = PdType.sp10_5,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
private fun CountBadge(count: Int, highlighted: Boolean, accent: Color = CrimsonPrimary) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) accent.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = count.toString(),
            fontSize = PdType.sp10_5,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xxs)
        )
    }
}
