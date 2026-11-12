package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Pd
import com.example.ui.theme.PdMotion
import com.example.ui.theme.PdRadius
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing

/**
 * کتابخانه‌ی اجزای پایه‌ی سامانه‌ی طراحی.
 *
 * صفحه‌ها برای دکمه‌ها، کارت‌ها، جداکننده‌ها و حالت‌های خالی/بارگذاری/خطا فقط از
 * همین اجزا استفاده کنند تا فاصله‌گذاری، گوشه‌ها، چینش و رفتار در کل برنامه
 * یکدست بماند. مقادیر پیش‌فرض همه از [com.example.ui.theme.PdSpacing] و
 * [com.example.ui.theme.PdRadius] می‌آیند.
 */

// ─────────────────────────────────────────────────────────────────────────────
// دکمه‌ها
// ─────────────────────────────────────────────────────────────────────────────

/** دکمه‌ی اصلی (پررنگ). فقط برای یک اقدام محوری در هر صفحه. */
@Composable
fun PdPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = PdShapes.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        modifier = modifier.heightIn(min = PdSize.buttonHeight)
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(PdSize.iconSm),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(PdSpacing.sm))
        } else if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(PdSize.iconMd))
            Spacer(Modifier.width(PdSpacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** دکمه‌ی کم‌رنگ (تُنال) برای اقدامات محوری فرعی. */
@Composable
fun PdSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = PdShapes.md,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        ),
        modifier = modifier.heightIn(min = PdSize.buttonHeight)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(PdSize.iconMd))
            Spacer(Modifier.width(PdSpacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** دکمه‌ی خط‌دار برای اقدامات کم‌اهمیت یا حذف. */
@Composable
fun PdOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = PdShapes.md,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        ),
        modifier = modifier.heightIn(min = PdSize.buttonHeight)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(PdSize.iconMd))
            Spacer(Modifier.width(PdSpacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** دکمه‌ی متنی بدون کادر، برای پیوندها و انصراف. */
@Composable
fun PdTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = PdShapes.sm,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        ),
        modifier = modifier.heightIn(min = PdSize.buttonHeightSm)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * دکمه‌ی آیکونی با ناحیه‌ی لمسی حداقل ٤٨dp.
 *
 * ناحیه‌ی لمس همیشه [PdSize.touchTarget] است؛ [visualSize] فقط اندازه‌ی سطح
 * دیداری را تعیین می‌کند تا در فهرست‌های چگال و کارت‌های شبکه‌ای جا کمتر بگیرد.
 */
@Composable
fun PdIconAction(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = Color.Transparent,
    visualSize: Dp = PdSize.touchTarget,
    iconSize: Dp = PdSize.iconLg,
    badge: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(PdSize.touchTarget)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.size(visualSize)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = if (enabled) tint else tint.copy(alpha = 0.4f),
                    modifier = Modifier.size(iconSize)
                )
                if (badge) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// کارت و جداکننده
// ─────────────────────────────────────────────────────────────────────────────

/**
 * کارت استاندارد: سطح [Pd.card]، گوشه‌ی [PdRadius.lg] و یک حاشیه‌ی ۱dp.
 * سایه‌ی اضافه عمداً ندارد تا پله‌ی عمق فقط با رنگ و حاشیه ساخته شود.
 */
@Composable
fun PdCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = PdShapes.lg,
        color = com.example.ui.theme.Pd.card,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) { content() }
}

/**
 * متن چپ‌به‌راست (لاتین) داخل چیدمان راست‌به‌چپ.
 *
 * نام‌های علمی، سرده‌ها، اسلاگ‌ها و نشانی‌ها باید در جهت خودشان باقی بمانند وگرنه
 * ترکیب فارسی و انگلیسی به‌هم می‌ریزد. همین کامپوننت را برای فیلدهای لاتین
 * (نام انگلیسی، agent_en، نشانی وب) استفاده کنید.
 */
@Composable
fun PdLtrText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            style = style
        )
    }
}

/** تیتر بخش: عنوان کوچک پررنگ با یک اقدام اختیاری در انتهای خط. */
@Composable
fun PdSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PdSize.touchTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(PdSize.iconMd)
                )
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

/** جداکننده‌ی ظریف بین بخش‌ها. */
@Composable
fun PdDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// حالت‌های صفحه
// ─────────────────────────────────────────────────────────────────────────────

/** حالت خالی: آیکون، تیتر، توضیح و یک اقدام اختیاری. */
@Composable
fun PdEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PdSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(PdSize.iconLg)
            )
        }
        Spacer(Modifier.height(PdSpacing.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (message != null) {
            Spacer(Modifier.height(PdSpacing.sm))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(PdSpacing.xl))
            PdSecondaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** حالت بارگذاری: اسپینر و پیام، بدون لرزش چیدمان. */
@Composable
fun PdLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PdSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(PdSize.iconLg)
        )
        if (message != null) {
            Spacer(Modifier.height(PdSpacing.lg))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** حالت خطا با اقدام تلاش مجدد. */
@Composable
fun PdErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    PdEmptyState(
        icon = Icons.Default.ErrorOutline,
        title = message,
        modifier = modifier,
        actionLabel = if (onRetry != null) "تلاش دوباره" else null,
        onAction = onRetry
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ورود با گوگل
// ─────────────────────────────────────────────────────────────────────────────

/** دکمه‌ی استاندارد ورود با گوگل با آرم چهاررنگ برند. */
@Composable
fun PdGoogleButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "ورود با گوگل"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        shape = PdShapes.md,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ),
        modifier = modifier.heightIn(min = PdSize.buttonHeight)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(PdSize.iconSm)
            )
        } else {
            GoogleLogo(Modifier.size(PdSize.iconMd))
        }
        Spacer(Modifier.width(PdSpacing.md))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// نشان‌ها
// ─────────────────────────────────────────────────────────────────────────────

/** نشان وضعیت رنگی (موفقیت، خطا، اطلاعات) با کنتراست متن روی زمینه. */
@Composable
fun PdStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(
        modifier = modifier,
        shape = PdShapes.pill,
        color = containerColor,
        contentColor = color
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = PdSpacing.md, vertical = PdSpacing.xs)
        )
    }
}

/**
 * چیپ فیلتر سامانه با رنگ‌های زنده و تغییر نرم حالت.
 *
 * چیپ‌های پیش‌فرض Material 3 در حالت غیرفعال معمولاً روی `surfaceVariant` با
 * حاشیه‌ی کم‌رنگ نشسته و با رنگ خاکستری نمایش داده می‌شوند که دشدار به نظر
 * می‌رسد؛ و چون رنگ‌ها به‌صورت ثابت اعمال می‌شوند، رفتن/آمدن حالت انتخاب
 * یک‌باره و بدون انتقال نرم عوض می‌شود.
 *
 * اینجا هر سه رنگ (زمینه، محتوا، حاشیه) با [animateColorAsState] نرم جابه‌جا
 * می‌شوند و حالت انتخاب‌شده با رنگ برند (یا [accent] دلخواه) و حاشیه‌ی هم‌رنگ
 * مشخص می‌شود.
 */
@Composable
fun PdFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val outline = MaterialTheme.colorScheme.outline
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface

    val container by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.18f) else surface,
        animationSpec = tween(PdMotion.fast),
        label = "chipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) accent else onSurfaceVariant,
        animationSpec = tween(PdMotion.fast),
        label = "chipContent"
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.9f) else outline.copy(alpha = 0.7f),
        animationSpec = tween(PdMotion.fast),
        label = "chipBorder"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        shape = PdShapes.md,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = container,
            labelColor = content,
            iconColor = content,
            selectedContainerColor = container,
            selectedLabelColor = content,
            selectedLeadingIconColor = content
        ),
        border = BorderStroke(1.dp, border)
    )
}

/**
 * کلید کشویی سامانه.
 *
 * حالت روشن با رنگ برند، حالت خاموش با حاشیه‌ی قابل‌مشاهده و دسته‌ای روشن تا
 * کلید هرگز «خاکستری و مرده» دیده نشود.
 */
@Composable
fun PdSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isDark = Pd.palette.isDark
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/**
 * کارتِ متن با انیمیشن ارتفاع نرم؛ برای باز/بسته‌شدن بخش‌ها بدون پرش چیدمان.
 */
@Composable
fun PdExpandableCard(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    PdCard(modifier = modifier.animateContentSize(tween(durationMillis = com.example.ui.theme.PdMotion.normal))) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = PdSize.touchTarget)
                    .padding(horizontal = PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) { header() }
            if (expanded) {
                PdDivider(Modifier.padding(horizontal = PdSpacing.lg))
                Box(Modifier.padding(PdSpacing.lg)) { content() }
            }
        }
    }
}
