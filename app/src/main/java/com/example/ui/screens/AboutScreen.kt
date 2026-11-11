package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PdSpacing.lg)
            .verticalScroll(rememberScrollState())
            .testTag("about_screen")
    ) {
        Spacer(modifier = Modifier.height(PdSpacing.md))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(PdShapes.md)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, CrimsonPrimary.copy(alpha = 0.5f), PdShapes.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔬", fontSize = PdType.sp22)
                }

                Column {
                    Text(
                        text = "درباره پاتودکس (Pathodex)",
                        fontSize = PdType.sp17,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مرجع هوشمند و تخصصی عوامل بیماری‌زا",
                        fontSize = PdType.sp11_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FilledTonalButton(
                onClick = onBack,
                shape = PdShapes.md
            ) {
                Text("بازگشت", fontSize = PdType.sp12, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))

        // Description Card
        ElevatedCard(
            shape = PdShapes.xl,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), PdShapes.xl)
        ) {
            Column(modifier = Modifier.padding(PdSpacing.roomy)) {
                Text(
                    text = "سامانه آموزشی و بالینی پاتودکس",
                    fontSize = PdType.sp15,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )

                Spacer(modifier = Modifier.height(PdSpacing.compact))

                Text(
                    text = "اپلیکیشن همراه پاتودکس با هدف تسهیل یادگیری، مرور هدفمند فلش‌کارت‌های سه‌بعدی و خودآزمایی بالینی دانشجویان و پژوهشگران علوم پزشکی، میکروبیولوژی و بهداشت طراحی شده است.\n\nاین سامانه مستقیماً به سرور پایگاه داده پاتودکس متصل بوده و علاوه بر همگام‌سازی ابری یادداشت‌ها و تعاملات، با قابلیت کشینگ هوشمند امکان مطالعه آفلاین را برای تمامی کاربران فراهم می‌سازد.",
                    fontSize = PdType.sp13,
                    lineHeight = PdType.sp23,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))

        // Contact Channels
        Text(
            text = "راه‌های ارتباطی و گزارش بازخورد",
            fontSize = PdType.sp14,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(PdSpacing.compact))

        // Email
        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ohioxoiho@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "بازخورد اپلیکیشن پاتودکس")
                    }
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier.padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.cozy)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(PdShapes.md)
                        .background(CrimsonPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("پست الکترونیکی", fontSize = PdType.sp11_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("ohioxoiho@gmail.com", fontSize = PdType.sp13_5, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.compact))

        // Telegram Channel
        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/patogenedex"))
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier.padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.cozy)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(PdShapes.md)
                        .background(com.example.ui.theme.Pd.info.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = com.example.ui.theme.Pd.info, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("کانال تلگرام پاتودکس", fontSize = PdType.sp11_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("@PathoDex", fontSize = PdType.sp13_5, fontWeight = FontWeight.Bold, color = com.example.ui.theme.Pd.info)
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.compact))

        // Web Server
        ElevatedCard(
            shape = PdShapes.lg,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pathodex.ir"))
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier.padding(PdSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PdSpacing.cozy)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(PdShapes.md)
                        .background(CrimsonPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("وب‌سایت و وب‌سرویس رسمی", fontSize = PdType.sp11_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("https://pathodex.ir", fontSize = PdType.sp13_5, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.scrollBottomInset))
    }
}
