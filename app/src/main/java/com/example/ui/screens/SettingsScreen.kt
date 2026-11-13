package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Disease
import com.example.data.model.User
import com.example.ui.components.PdSwitch
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeAccents
import com.example.ui.theme.tone
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType

@Composable
fun SettingsScreen(
    currentUser: User?,
    themeMode: String,
    accentId: String,
    isCompactListMode: Boolean,
    blurImages: Boolean,
    deepLinksEnabled: Boolean = false,
    allDiseases: List<Disease>,
    isLoading: Boolean,
    isServerConnected: Boolean? = null,
    lastSyncTime: String? = null,
    onSetThemeMode: (String) -> Unit,
    onSetAccent: (String) -> Unit,
    onSetCompactMode: (Boolean) -> Unit,
    onToggleBlur: () -> Unit,
    onSetDeepLinksEnabled: (Boolean) -> Unit = {},
    onRefreshData: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    onOpenDossier: (Disease) -> Unit,
    onOpenTaxonomy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalDiseases = remember(allDiseases) { allDiseases.size }
    val learnedCount = remember(allDiseases) { allDiseases.count { it.is_learned == true } }
    val favoriteCount = remember(allDiseases) { allDiseases.count { it.is_favorite == true } }
    val bookmarkCount = remember(allDiseases) { allDiseases.count { it.is_bookmarked == true } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PdSpacing.lg)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(PdSpacing.cozy))

        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(PdShapes.md)
                    .background(CrimsonPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "تنظیمات و سفارشی‌سازی",
                    fontSize = PdType.sp20,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تنظیمات ظاهر، چیدمان، حریم بالینی و دیتابیس محلی",
                    fontSize = PdType.sp11_5,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))

        // SECTION 1: User Account & Authentication
        SettingsSectionTitle(title = "حساب کاربری و وضعیت اتصال", icon = Icons.Default.AccountCircle)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(PdSpacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.md)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentUser != null) CrimsonPrimary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentUser == null) "👤"
                                    else if (currentUser.role == "owner") "👑"
                                    else "🩺",
                                    fontSize = PdType.sp20
                                )
                            }
                        }

                        Column {
                            Text(
                                text = currentUser?.username ?: "کاربر مهمان (آفلاین)",
                                fontSize = PdType.sp15,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val roleText = when (currentUser?.role) {
                                "owner" -> "مالک سامانه (Owner)"
                                "admin" -> "مدیر بالینی (Admin)"
                                "user" -> "کاربر تایید شده (Member)"
                                else -> "دسترسی آزاد به دیتابیس بالینی"
                            }
                            Text(
                                text = roleText,
                                fontSize = PdType.sp11,
                                color = CrimsonPrimary
                            )
                        }
                    }

                    if (currentUser == null) {
                        Button(
                            onClick = onOpenAuth,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = PdShapes.md
                        ) {
                            Text("ورود / ثبت‌نام", fontSize = PdType.sp11_5, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onLogout,
                            shape = PdShapes.md
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(PdSpacing.xs))
                            Text("خروج", fontSize = PdType.sp11_5)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 2: Layout & Display Density
        SettingsSectionTitle(title = "چیدمان و تراکم اطلاعات", icon = Icons.Default.ViewAgenda)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(PdSpacing.lg)) {
                Text(
                    text = "شیوه نمایش فهرست پاتوژن‌ها:",
                    fontSize = PdType.sp13,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(PdSpacing.tight))
                Text(
                    text = "برای مرور سریع ده‌ها و صدها بیماری، حالت فشرده پیشنهاد می‌شود.",
                    fontSize = PdType.sp11,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(PdSpacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
                ) {
                    // Option 1: Compact List (Recommended)
                    Surface(
                        shape = PdShapes.md,
                        color = if (isCompactListMode) CrimsonPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isCompactListMode) 1.5.dp else 1.dp,
                            color = if (isCompactListMode) CrimsonPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(PdShapes.md)
                            .clickable { onSetCompactMode(true) }
                    ) {
                        Column(
                            modifier = Modifier.padding(PdSpacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = null,
                                tint = if (isCompactListMode) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.tight))
                            Text(
                                text = "فهرست فشرده",
                                fontSize = PdType.sp12_5,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompactListMode) CrimsonPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "۶-۸ مورد در صفحه (پیشنهادی)",
                                fontSize = PdType.sp9_5,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Option 2: Cards Grid
                    Surface(
                        shape = PdShapes.md,
                        color = if (!isCompactListMode) CrimsonPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isCompactListMode) 1.5.dp else 1.dp,
                            color = if (!isCompactListMode) CrimsonPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(PdShapes.md)
                            .clickable { onSetCompactMode(false) }
                    ) {
                        Column(
                            modifier = Modifier.padding(PdSpacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = if (!isCompactListMode) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(PdSpacing.tight))
                            Text(
                                text = "کارت‌های دو ستونه",
                                fontSize = PdType.sp12_5,
                                fontWeight = FontWeight.Bold,
                                color = if (!isCompactListMode) CrimsonPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "نمایش بزرگ عکس‌ها",
                                fontSize = PdType.sp9_5,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 3: Taxonomy Tree (mirrors the website's taxonomy page)
        SettingsSectionTitle(title = "درخت تاکسونومی", icon = Icons.Default.AccountTree)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenTaxonomy() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "توزیع سلسله‌مراتبی عوامل بیماری‌زا",
                        fontSize = PdType.sp13_5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.xxs))
                    Text(
                        text = "مرور دسته، رده و خانواده پاتوژن‌ها به‌صورت درخت‌واره و تاشو",
                        fontSize = PdType.sp11,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CrimsonPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "باز کردن درخت تاکسونومی",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 4: Theme & Visual Appearance
        SettingsSectionTitle(title = "تم و ظاهر بصری", icon = Icons.Default.DarkMode)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(PdSpacing.lg)) {
                Text(
                    text = "انتخاب حالت رنگی سامانه:",
                    fontSize = PdType.sp13,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(PdSpacing.compact))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    ThemeOptionButton(
                        title = "سیستم",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = themeMode == "system",
                        onClick = { onSetThemeMode("system") },
                        modifier = Modifier.weight(1f)
                    )

                    ThemeOptionButton(
                        title = "حالت تیره",
                        icon = Icons.Default.DarkMode,
                        isSelected = themeMode == "dark",
                        onClick = { onSetThemeMode("dark") },
                        modifier = Modifier.weight(1f)
                    )

                    ThemeOptionButton(
                        title = "حالت روشن",
                        icon = Icons.Default.LightMode,
                        isSelected = themeMode == "light",
                        onClick = { onSetThemeMode("light") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // Accent color picker — رنگ برند در سراسر برنامه عوض می‌شود
                Text(
                    text = "رنگ accent سامانه:",
                    fontSize = PdType.sp13,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(PdSpacing.xs))
                Text(
                    text = "این رنگ روی دکمه‌ها، نشانگرها و تاکیدها در سراسر برنامه اعمال می‌شود.",
                    fontSize = PdType.sp11,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(PdSpacing.md))

                ThemeAccents.chunked(4).forEach { rowAccents ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
                    ) {
                        rowAccents.forEach { accent ->
                            AccentOption(
                                accent = accent,
                                isSelected = accentId == accent.id,
                                onClick = { onSetAccent(accent.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(PdSpacing.compact))
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 4: Clinical Image Blur Protocol
        SettingsSectionTitle(title = "پروتکل ایمنی و فیلتر بالینی", icon = Icons.Default.Security)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(PdSpacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تار کردن خودکار تصاویر بالینی حساس",
                            fontSize = PdType.sp13,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(PdSpacing.xs))
                        Text(
                            text = "جهت حفظ حریم شخصی در اماکن عمومی، تصاویر زخم‌ها و ضایعات عفونی تار نمایش داده شده و با لمس شما آشکار می‌شوند.",
                            fontSize = PdType.sp11,
                            lineHeight = PdType.sp17,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(PdSpacing.compact))

                    PdSwitch(
                        checked = blurImages,
                        onCheckedChange = { onToggleBlur() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 4b: پیوندهای وب — انتخاب کاربر: اپلیکیشن یا مرورگر
        SettingsSectionTitle(title = "پیوندهای وب", icon = Icons.Default.Link)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "باز کردن پیوندهای pathodex.ir در اپلیکیشن",
                        fontSize = PdType.sp13,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.xs))
                    Text(
                        text = "با روشن بودن این کلید، پیوندهای pathodex.ir در اپلیکیشن باز می‌شوند. اگر خاموش باشد، همان پیوندها در مرورگر وب باز می‌شوند و اپلیکیشن دست‌نخورده می‌ماند.",
                        fontSize = PdType.sp11,
                        lineHeight = PdType.sp17,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(PdSpacing.compact))

                PdSwitch(
                    checked = deepLinksEnabled,
                    onCheckedChange = onSetDeepLinksEnabled
                )
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 5: Server Connection Status & Manual Sync
        SettingsSectionTitle(title = "ارتباط با سرور و همگام‌سازی", icon = Icons.Default.CloudSync)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PdSpacing.lg)
            ) {
                // Connection indicator row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (isServerConnected) {
                                        true -> EmeraldLearned
                                        false -> com.example.ui.theme.Pd.warning
                                        null -> MaterialTheme.colorScheme.outline
                                    }
                                )
                        )
                        Text(
                            text = when (isServerConnected) {
                                true -> "ارتباط با سرور برقرار است (آنلاین)"
                                false -> "حالت آفلاین (پایگاه داده محلی)"
                                null -> "در حال بررسی ارتباط با سرور..."
                            },
                            fontSize = PdType.sp13,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = PdShapes.sm,
                        color = (if (isServerConnected == true) EmeraldLearned else CrimsonPrimary).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$totalDiseases پاتوژن فعال",
                            fontSize = PdType.sp11,
                            fontWeight = FontWeight.Bold,
                            color = if (isServerConnected == true) EmeraldLearned else CrimsonPrimary,
                            modifier = Modifier.padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                Text(
                    text = if (lastSyncTime != null) {
                        "همگام‌سازی خودکار در پس‌زمینه فعال است. آخرین بررسی: $lastSyncTime"
                    } else {
                        "داده‌ها به‌طور خودکار در پس‌زمینه با سرور همگام می‌شوند. در صورت عدم اتصال، اطلاعات از حافظه دستگاه خوانده می‌شوند."
                    },
                    fontSize = PdType.sp11,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = PdType.sp16
                )

                Spacer(modifier = Modifier.height(PdSpacing.cozy))

                FilledTonalButton(
                    onClick = onRefreshData,
                    enabled = !isLoading,
                    shape = PdShapes.md,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CrimsonPrimary.copy(alpha = 0.12f),
                        contentColor = CrimsonPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("manual_sync_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = CrimsonPrimary
                        )
                        Spacer(modifier = Modifier.width(PdSpacing.sm))
                        Text(
                            text = "در حال همگام‌سازی...",
                            fontSize = PdType.sp12_5,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(PdSpacing.sm))
                        Text(
                            text = "همگام‌سازی دستی با سرور",
                            fontSize = PdType.sp12_5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.roomy))

        // SECTION 6: Minimal Website Reference Card (just the URL)
        SettingsSectionTitle(title = "وب‌سایت مرجع", icon = Icons.Default.Info)

        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pathodex.ir"))
                    context.startActivity(browserIntent)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "pathodex.ir",
                        fontSize = PdType.sp14,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                    Spacer(modifier = Modifier.height(PdSpacing.xxs))
                    Text(
                        text = "مشاهده سامانه در مرورگر",
                        fontSize = PdType.sp11,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CrimsonPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "باز کردن وب‌سایت",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.scrollBottomInset))
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight),
        modifier = Modifier.padding(bottom = PdSpacing.sm, start = PdSpacing.xxs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CrimsonPrimary,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = title,
            fontSize = PdType.sp13,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PdShapes.md,
        color = if (isSelected) CrimsonPrimary.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) CrimsonPrimary else Color.Transparent
        ),
        modifier = modifier
            .clip(PdShapes.md)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = PdSpacing.compact, horizontal = PdSpacing.tight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(PdSpacing.xs))
            Text(
                text = title,
                fontSize = PdType.sp11,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** یک گزینه‌ی رنگ accent با دایره‌ی نمونه و برچسب فارسی. */
@Composable
private fun AccentOption(
    accent: ThemeAccent,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        shape = PdShapes.md,
        color = if (isSelected) primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) primary else Color.Transparent
        ),
        modifier = modifier
            .clip(PdShapes.md)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = PdSpacing.compact, horizontal = PdSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accent.tone.primary)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(PdSpacing.xs))
            Text(
                text = accent.labelFa,
                fontSize = PdType.sp10_5,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
