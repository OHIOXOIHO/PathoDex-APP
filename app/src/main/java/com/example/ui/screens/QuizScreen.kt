package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Disease
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdType
import com.example.ui.viewmodel.QuizQuestion

@Composable
fun QuizScreen(
    question: QuizQuestion?,
    selectedAnswer: String?,
    score: Int,
    total: Int,
    streak: Int,
    onSelectOption: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onOpenDossier: (Disease) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PdSpacing.lg)
            .verticalScroll(rememberScrollState())
            .testTag("quiz_screen")
    ) {
        Spacer(modifier = Modifier.height(PdSpacing.md))

        // Top Metrics Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "خودآزمایی بالینی پاتوژن‌ها",
                    fontSize = PdType.sp18,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "پرسش‌های چهارگزینه‌ای از داده‌های بالینی",
                    fontSize = PdType.sp11_5,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Metrics Badges
            Row(horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)) {
                // Streak Badge
                Surface(
                    shape = PdShapes.md,
                    color = AmberBookmark.copy(alpha = 0.18f),
                    modifier = Modifier.padding(vertical = PdSpacing.xxs)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.tight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "زنجیره درست",
                            tint = AmberBookmark,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "$streak",
                            fontSize = PdType.sp13,
                            fontWeight = FontWeight.Black,
                            color = AmberBookmark
                        )
                    }
                }

                // Score Badge
                Surface(
                    shape = PdShapes.md,
                    color = EmeraldLearned.copy(alpha = 0.18f),
                    modifier = Modifier.padding(vertical = PdSpacing.xxs)
                ) {
                    Text(
                        text = "$score از $total",
                        fontSize = PdType.sp13,
                        fontWeight = FontWeight.Black,
                        color = EmeraldLearned,
                        modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.tight)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.lg))

        if (question == null) {
            ElevatedCard(
                shape = PdShapes.xl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "در حال بارگذاری سوالات از بانک اطلاعاتی...",
                        fontSize = PdType.sp13_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            ElevatedCard(
                shape = PdShapes.xl,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), PdShapes.xl)
                    .testTag("quiz_card")
            ) {
                Column(modifier = Modifier.padding(PdSpacing.xl)) {
                    // Question Type Badge & Skip Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = PdShapes.md,
                            color = CrimsonPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = question.badge,
                                fontSize = PdType.sp11_5,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary,
                                modifier = Modifier.padding(horizontal = PdSpacing.compact, vertical = PdSpacing.xs)
                            )
                        }

                        OutlinedButton(
                            onClick = onNextQuestion,
                            shape = PdShapes.md,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("رد کردن", fontSize = PdType.sp11, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(PdSpacing.xs))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رد کردن", modifier = Modifier.size(15.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.cozy))

                    // Visual Clue Box (Clinical Case)
                    if (!question.clueImage.isNullOrBlank() || !question.clueExcerpt.isNullOrBlank()) {
                        Surface(
                            shape = PdShapes.lg,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = PdSpacing.cozy)
                        ) {
                            Row(
                                modifier = Modifier.padding(PdSpacing.md),
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!question.clueImage.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(PdShapes.md)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), PdShapes.md)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(question.clueImage)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "سرنخ بالینی",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                if (!question.clueExcerpt.isNullOrBlank()) {
                                    Text(
                                        text = question.clueExcerpt,
                                        fontSize = PdType.sp11_5,
                                        lineHeight = PdType.sp18,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Question Title
                    Text(
                        text = question.title,
                        fontSize = PdType.sp16,
                        fontWeight = FontWeight.Bold,
                        lineHeight = PdType.sp24,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.roomy))

                    // 4 Options Styled as Real Android Choice Buttons
                    question.options.forEachIndexed { index, option ->
                        val isAnswered = selectedAnswer != null
                        val isThisOptionSelected = selectedAnswer == option
                        val isCorrectOption = option == question.correctAnswer

                        val optionContainerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            isCorrectOption -> EmeraldLearned.copy(alpha = 0.16f)
                            isThisOptionSelected && !isCorrectOption -> CrimsonPrimary.copy(alpha = 0.16f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }

                        val optionBorderColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                            isCorrectOption -> EmeraldLearned
                            isThisOptionSelected && !isCorrectOption -> CrimsonPrimary
                            else -> Color.Transparent
                        }

                        Surface(
                            shape = PdShapes.md,
                            color = optionContainerColor,
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.5.dp, optionBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = PdSpacing.xs)
                                .clip(PdShapes.md)
                                .clickable(enabled = !isAnswered) { onSelectOption(option) }
                                .testTag("quiz_option_$index")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = PdSpacing.cozy, vertical = PdSpacing.cozy),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCorrectOption && isAnswered -> EmeraldLearned.copy(alpha = 0.22f)
                                                    isThisOptionSelected && isAnswered -> CrimsonPrimary.copy(alpha = 0.22f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = PdType.sp11_5,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isCorrectOption && isAnswered -> EmeraldLearned
                                                isThisOptionSelected && isAnswered -> CrimsonPrimary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }

                                    Text(
                                        text = option,
                                        fontSize = PdType.sp13,
                                        fontWeight = if (isCorrectOption && isAnswered) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isAnswered) {
                                    if (isCorrectOption) {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldLearned,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Check, contentDescription = "درست", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else if (isThisOptionSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = CrimsonPrimary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Close, contentDescription = "نادرست", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Answered Feedback Panel
                    AnimatedVisibility(visible = selectedAnswer != null) {
                        val isCorrect = selectedAnswer == question.correctAnswer
                        Surface(
                            shape = PdShapes.lg,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = PdSpacing.lg)
                        ) {
                            Column(modifier = Modifier.padding(PdSpacing.lg)) {
                                Text(
                                    text = if (isCorrect) "پاسخ صحیح است! احسنت ✓" else "پاسخ نادرست!",
                                    fontSize = PdType.sp13_5,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) EmeraldLearned else CrimsonPrimary
                                )

                                if (!isCorrect) {
                                    Text(
                                        text = "پاسخ درست: ${question.correctAnswer}",
                                        fontSize = PdType.sp12_5,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = PdSpacing.xxs)
                                    )
                                }

                                Spacer(modifier = Modifier.height(PdSpacing.cozy))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalButton(
                                        onClick = { onOpenDossier(question.targetDisease) },
                                        shape = PdShapes.md,
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text("مشاهده پرونده پاتوژن", fontSize = PdType.sp12, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onNextQuestion,
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                        shape = PdShapes.md,
                                        modifier = Modifier.height(44.dp).testTag("quiz_next_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                                        ) {
                                            Text("سوال بعدی", fontSize = PdType.sp12_5, fontWeight = FontWeight.Bold, color = Color.White)
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بعدی", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PdSpacing.scrollBottomInset))
    }
}
