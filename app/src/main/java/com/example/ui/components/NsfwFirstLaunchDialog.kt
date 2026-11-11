package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType

@Composable
fun NsfwFirstLaunchDialog(
    onAccept: (enableBlur: Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = {}, // Force decision
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = PdShapes.xxl,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(PdSpacing.lg)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), PdShapes.xxl)
                .testTag("nsfw_dialog")
        ) {
            Column(
                modifier = Modifier.padding(PdSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(PdShapes.lg)
                        .background(CrimsonPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "هشدار محتوای بالینی",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(PdSpacing.cozy))

                Text(
                    text = "پروتکل تصاویر بالینی و پاتولوژی",
                    fontSize = PdType.sp11,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )

                Text(
                    text = "اطلاعیه محتوای پزشکی",
                    fontSize = PdType.sp18,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = PdSpacing.xs, bottom = PdSpacing.sm)
                )

                Text(
                    text = "سامانه پاتودکس شامل تصاویر واقعی از ضایعات بالینی، بافت‌شناسی و نمونه‌های میکروسکوپی عوامل بیماری‌زا است. نحوه نمایش اولیه تصاویر را انتخاب کنید:",
                    fontSize = PdType.sp12_5,
                    lineHeight = PdType.sp20,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(PdSpacing.xl))

                Button(
                    onClick = { onAccept(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = PdShapes.md,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("accept_normal_view_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ورود و نمایش عادی تصاویر",
                            fontSize = PdType.sp12_5,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                OutlinedButton(
                    onClick = { onAccept(true) },
                    shape = PdShapes.md,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("accept_blur_view_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ورود با فیلتر تارکننده (Blur)",
                            fontSize = PdType.sp12,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
