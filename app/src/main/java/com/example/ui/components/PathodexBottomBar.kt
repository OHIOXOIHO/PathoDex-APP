package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdSize
import com.example.ui.viewmodel.NavTab

/**
 * نوار پیمایش پایین به‌صورت استاندارد Material 3.
 *
 * فقط نشان و آیکون حالت انتخاب‌شده با رنگ برند درخشند؛ بقیه‌ی نشان‌ها خاکستری
 * ملایم‌اند تا تنها یک مقدار برجسته در نوار وجود داشته باشد.
 */
@Composable
fun PathodexBottomBar(
    currentTab: NavTab,
    compareCount: Int,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        modifier = modifier.testTag("pathodex_bottom_nav")
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = CrimsonPrimary,
            indicatorColor = CrimsonPrimary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val items = listOf(
            Triple(NavTab.EXPLORER, Icons.Default.GridView, "پاتوژن‌ها"),
            Triple(NavTab.FLASHCARDS, Icons.Default.Style, "فلش‌کارت"),
            Triple(NavTab.QUIZ, Icons.Default.Quiz, "خودآزمایی"),
            Triple(NavTab.COMPARE, Icons.Default.Scale, "مقایسه"),
            Triple(NavTab.SETTINGS, Icons.Default.Settings, "تنظیمات")
        )

        items.forEach { (tab, icon, label) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == NavTab.COMPARE) {
                        BadgedBox(
                            badge = {
                                if (compareCount > 0) {
                                    Badge(
                                        containerColor = CrimsonPrimary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = "$compareCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(PdSize.iconLg)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(PdSize.iconLg)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = navColors,
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
