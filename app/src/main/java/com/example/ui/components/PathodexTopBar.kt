package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathodexTopBar(
    currentUser: User?,
    blurImages: Boolean,
    isLoading: Boolean,
    themeMode: String,
    onToggleBlur: () -> Unit,
    onToggleTheme: () -> Unit,
    onRefresh: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact),
                modifier = Modifier.testTag("brand_header")
            ) {
                // Microscope Brand Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(PdShapes.md)
                        .background(CrimsonPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔬", fontSize = PdType.sp18)
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "پاتو",
                            fontWeight = FontWeight.Black,
                            fontSize = PdType.sp18,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "دکس",
                            fontWeight = FontWeight.Black,
                            fontSize = PdType.sp18,
                            color = CrimsonPrimary
                        )
                    }
                    Text(
                        text = "مرجع هوشمند بالینی",
                        fontSize = PdType.sp10,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs),
                modifier = Modifier.padding(end = PdSpacing.xs)
            ) {
                // 1. Blur Filter Toggle
                FilledTonalIconButton(
                    onClick = onToggleBlur,
                    shape = PdShapes.md,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (blurImages) CrimsonPrimary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("blur_toggle_button")
                ) {
                    Icon(
                        imageVector = if (blurImages) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "فیلتر تارکننده",
                        tint = if (blurImages) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // 2. Theme Toggle (Dark / Light)
                FilledTonalIconButton(
                    onClick = onToggleTheme,
                    shape = PdShapes.md,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (themeMode == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "تغییر تم",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // 3. Refresh Action
                FilledTonalIconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    shape = PdShapes.md,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("refresh_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(17.dp),
                            strokeWidth = 2.dp,
                            color = CrimsonPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "بروزرسانی",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // 4. User Profile / Login
                if (currentUser == null) {
                    FilledTonalIconButton(
                        onClick = onOpenAuth,
                        shape = PdShapes.md,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = CrimsonPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "ورود کاربر",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = PdShapes.md,
                        color = CrimsonPrimary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(PdShapes.md)
                            .clickable { onOpenProfile() }
                            .testTag("user_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (currentUser.role == "owner") "👑" else "🩺",
                                fontSize = PdType.sp16
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}
