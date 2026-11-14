package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.data.model.SearchMatcher
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import com.example.ui.components.PdLtrText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    compareIds: List<Int>,
    allDiseases: List<Disease>,
    onToggleCompare: (Int) -> Unit,
    onClearCompare: () -> Unit,
    onOpenDossier: (Disease) -> Unit,
    modifier: Modifier = Modifier
) {
    val comparedDiseases = remember(compareIds, allDiseases) {
        compareIds.mapNotNull { id -> allDiseases.find { it.id == id } }
    }

    var pickerSlotIndex by remember { mutableStateOf<Int?>(null) }
    var pickerSearchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredPickerDiseases = remember(pickerSearchQuery, allDiseases) {
        // جستجوی خطاپذیر، مثل بقیه‌ی صفحه‌ها و سامانه‌ی وب
        val prepared = SearchMatcher.prepareQuery(pickerSearchQuery)
        if (prepared.empty) {
            allDiseases
        } else {
            val scored = allDiseases.mapNotNull { d ->
                val pool = listOfNotNull(
                    d.name_fa, d.name_en, d.agent_fa, d.agent_en, d.category,
                    d.family_fa, d.family_en, d.class_fa, d.class_en, d.genus_fa, d.genus_en
                ).joinToString(" ")
                val result = SearchMatcher.matchPrepared(prepared, SearchMatcher.indexPool(pool))
                if (result.match) d to result.score else null
            }
            scored.sortedByDescending { it.second }.map { it.first }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PdSpacing.cozy)
            .verticalScroll(rememberScrollState())
            .testTag("compare_screen")
    ) {
        Spacer(modifier = Modifier.height(PdSpacing.md))

        // Header and Clear Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مقایسه تطبیقی پاتوژن‌ها",
                    fontSize = PdType.sp18,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "بررسی همزمان و تفکیک ویژگی‌های تشخیصی",
                    fontSize = PdType.sp11_5,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (compareIds.isNotEmpty()) {
                FilledTonalButton(
                    onClick = onClearCompare,
                    shape = PdShapes.md,
                    modifier = Modifier.testTag("clear_compare_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(PdSpacing.tight))
                    Text("پاکسازی", fontSize = PdType.sp11_5, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))

        // Spacious 2-Slot Comparison Selector with VS Badge
        val d1 = comparedDiseases.getOrNull(0)
        val d2 = comparedDiseases.getOrNull(1)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
        ) {
            // Slot 1
            CompareSlotCard(
                slotTitle = "پاتوژن اول",
                disease = d1,
                onSelectClick = { pickerSlotIndex = 0 },
                onRemoveClick = { if (d1 != null) onToggleCompare(d1.id) },
                modifier = Modifier.weight(1f)
            )

            // VS Circle Badge
            Surface(
                shape = CircleShape,
                color = CrimsonPrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "VS",
                        fontSize = PdType.sp11,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Slot 2
            CompareSlotCard(
                slotTitle = "پاتوژن دوم",
                disease = d2,
                onSelectClick = { pickerSlotIndex = 1 },
                onRemoveClick = { if (d2 != null) onToggleCompare(d2.id) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // Matrix Comparison Card or Informative Prompt
        if (comparedDiseases.size < 2) {
            OutlinedCard(
                shape = PdShapes.xl,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PdSpacing.md)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PdSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.md))

                    Text(
                        text = if (comparedDiseases.isEmpty()) "دو پاتوژن را برای مقایسه انتخاب کنید"
                        else "پاتوژن دوم را برای شروع مقایسه انتخاب کنید",
                        fontSize = PdType.sp14_5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.tight))

                    Text(
                        text = "با لمس کادرهای بالا یا دکمه زیر، می‌توانید پاتوژن‌های مورد نظر خود را جستجو کرده و تفاوت‌های بالینی، تاکسونومیک و عاملی آن‌ها را بررسی کنید.",
                        fontSize = PdType.sp12,
                        lineHeight = PdType.sp18,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.cozy))

                    FilledTonalButton(
                        onClick = {
                            pickerSlotIndex = if (comparedDiseases.isEmpty()) 0 else 1
                        },
                        shape = PdShapes.md,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CrimsonPrimary.copy(alpha = 0.12f),
                            contentColor = CrimsonPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(PdSpacing.tight))
                        Text(
                            text = if (comparedDiseases.isEmpty()) "انتخاب پاتوژن اول" else "انتخاب پاتوژن دوم",
                            fontSize = PdType.sp12,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Detailed Side-by-Side Matrix
            ElevatedCard(
                shape = PdShapes.xl,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), PdShapes.xl)
            ) {
                Column {
                    // Header Row: Thumbnails + Names + Dossier Action
                    CompareRow(title = "عنوان و پرونده") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            comparedDiseases.forEach { d ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(PdSpacing.compact),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val img = d.images?.firstOrNull()
                                    if (img != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(PdShapes.md)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(img.resolvedUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(PdShapes.md)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🔬", fontSize = PdType.sp24)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(PdSpacing.tight))

                                    Text(
                                        text = d.category,
                                        fontSize = PdType.sp10,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )

                                    Text(
                                        text = d.name_fa,
                                        fontSize = PdType.sp13,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (!d.name_en.isNullOrBlank()) {
                                        PdLtrText(
                                            text = d.name_en,
                                            fontSize = PdType.sp10_5,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(PdSpacing.sm))

                                    FilledTonalButton(
                                        onClick = { onOpenDossier(d) },
                                        shape = PdShapes.sm,
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("مشاهده پرونده", fontSize = PdType.sp10_5)
                                    }
                                }
                            }
                        }
                    }

                    // Agent Row
                    CompareRow(title = "عامل بیماری‌زا") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            comparedDiseases.forEach { d ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(PdSpacing.compact)
                                ) {
                                    Text(
                                        text = d.agent_fa,
                                        fontSize = PdType.sp12_5,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary
                                    )
                                    if (!d.agent_en.isNullOrBlank()) {
                                    PdLtrText(
                                            text = d.agent_en,
                                            fontSize = PdType.sp11,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Taxonomy Row
                    CompareRow(title = "رده‌بندی تاکسونومی") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            comparedDiseases.forEach { d ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(PdSpacing.compact)
                                ) {
                                    Text(
                                        text = "خانواده: ${d.family_fa ?: d.family_en ?: "—"}",
                                        fontSize = PdType.sp11_5,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "رده: ${d.class_fa ?: d.class_en ?: "—"}",
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                    if (!d.genus_fa.isNullOrBlank()) {
                                        Text(
                                            text = "سرده: ${d.genus_fa}",
                                            fontSize = PdType.sp11,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = PdSpacing.xxs)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Transmission & Hosts Row
                    CompareRow(title = "راه انتقال و میزبان") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            comparedDiseases.forEach { d ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(PdSpacing.compact)
                                ) {
                                    Text(
                                        text = d.transmission.joinToString("، ") { it.name_fa ?: it.name_en ?: it.slug }
                                            .ifBlank { "—" },
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "میزبان: " + d.hosts.joinToString("، ") { it.name_fa ?: it.name_en ?: it.slug }
                                            .ifBlank { "—" },
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                    Text(
                                        text = "محدوده: ${d.hostScopeLabel}",
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                }
                            }
                        }
                    }

                    // Clinical Monograph Row
                    CompareRow(title = "خلاصه بالینی و علائم") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            comparedDiseases.forEach { d ->
                                Text(
                                    text = d.description_fa,
                                    fontSize = PdType.sp11_5,
                                    lineHeight = PdType.sp18,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(PdSpacing.compact)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))
    }

    // Searchable Disease Picker Modal Bottom Sheet
    if (pickerSlotIndex != null) {
        ModalBottomSheet(
            onDismissRequest = {
                pickerSlotIndex = null
                pickerSearchQuery = ""
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PdSpacing.lg)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب پاتوژن برای مقایسه",
                        fontSize = PdType.sp16,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { pickerSlotIndex = null }) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.compact))

                OutlinedTextField(
                    value = pickerSearchQuery,
                    onValueChange = { pickerSearchQuery = it },
                    placeholder = { Text("جستجوی نام یا عامل...", fontSize = PdType.sp12) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrimsonPrimary) },
                    trailingIcon = {
                        if (pickerSearchQuery.isNotBlank()) {
                            IconButton(onClick = { pickerSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = PdShapes.md,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(PdSpacing.md))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                ) {
                    items(filteredPickerDiseases, key = { it.id }) { item ->
                        val isAlreadySelected = compareIds.contains(item.id)
                        Surface(
                            shape = PdShapes.md,
                            color = if (isAlreadySelected) CrimsonPrimary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PdShapes.md)
                                .clickable {
                                    val currentSlot = pickerSlotIndex
                                    if (currentSlot != null) {
                                        val existingAtSlot = comparedDiseases.getOrNull(currentSlot)
                                        if (existingAtSlot != null && existingAtSlot.id != item.id) {
                                            onToggleCompare(existingAtSlot.id)
                                        }
                                        if (!compareIds.contains(item.id)) {
                                            onToggleCompare(item.id)
                                        }
                                    }
                                    pickerSlotIndex = null
                                    pickerSearchQuery = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = PdSpacing.md, vertical = PdSpacing.compact),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name_fa,
                                        fontSize = PdType.sp13_5,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.category} • عامل: ${item.agent_fa}",
                                        fontSize = PdType.sp11,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isAlreadySelected) {
                                    Surface(
                                        shape = PdShapes.xs,
                                        color = CrimsonPrimary
                                    ) {
                                        Text(
                                            text = "انتخاب‌شده",
                                            fontSize = PdType.sp9_5,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = PdSpacing.tight, vertical = PdSpacing.xxs)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareSlotCard(
    slotTitle: String,
    disease: Disease?,
    onSelectClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PdShapes.lg,
        color = if (disease != null) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (disease != null) 1.5.dp else 1.dp,
            color = if (disease != null) CrimsonPrimary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        shadowElevation = if (disease != null) 2.dp else 0.dp,
        modifier = modifier
            .height(115.dp)
            .clip(PdShapes.lg)
            .clickable { onSelectClick() }
    ) {
        if (disease != null) {
            Box(modifier = Modifier.fillMaxSize().padding(PdSpacing.compact)) {
                // Remove Button
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = slotTitle,
                        fontSize = PdType.sp10,
                        fontWeight = FontWeight.Medium,
                        color = CrimsonPrimary
                    )

                    Column {
                        Text(
                            text = disease.name_fa,
                            fontSize = PdType.sp13,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = disease.agent_fa,
                            fontSize = PdType.sp11,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "لمس جهت تغییر",
                        fontSize = PdType.sp9_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PdSpacing.compact),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = CrimsonPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(PdSpacing.tight))
                Text(
                    text = slotTitle,
                    fontSize = PdType.sp12,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "لمس جهت انتخاب",
                    fontSize = PdType.sp9_5,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompareRow(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = PdSpacing.cozy, vertical = PdSpacing.tight)
        ) {
            Text(
                text = title,
                fontSize = PdType.sp11_5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}
