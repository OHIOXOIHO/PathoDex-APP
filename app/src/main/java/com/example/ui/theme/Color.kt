package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// سامانه‌ی طراحی مسیرهای — «میدنایت رز / بنفش»
//
// همه‌ی رنگ‌های ساختاری (پس‌زمینه، سطح، کارت، حاشیه، متن، موفقیت، خطا) در یک
// جا تعریف می‌شوند و از طریق LocalPdPalette در دسترس همه‌ی صفحه‌ها قرار می‌گیرند.
// صفحه‌ها هرگز نباید رنگ ثابت در خودشان تعریف کنند.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * مجموعه‌ی کامل رنگ‌های ساختاری یک تم.
 *
 * @param card سطح کارت‌ها؛ یک پله روشن‌تر از [surface] تا عمق لایه‌ای دیده شود.
 * @param border رنگ جداکننده‌ها و حاشیه‌ی کارت‌ها.
 * @param textDisabled متن و آیکون‌های غیرفعال؛ کنتراست پایین‌تر از متن معمولی.
 */
data class PdPalette(
    val isDark: Boolean,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val card: Color,
    val onCard: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val bookmark: Color,
    /** آبی اطلاعات / پیوندهای خارجی (مثل کانال تلگرام). */
    val info: Color,
    /** کهربایی هشدار (مثل نشان «اتصال برقرار نیست»). */
    val warning: Color,
    val scrim: Color
)

// ── تم تیره: دقیقاً پالت خواسته‌شده‌ی «میدنایت رز / بنفش» ─────────────────────
fun darkPdPalette() = PdPalette(
    isDark = true,
    background = Color(0xFF1B1B2E),
    onBackground = Color(0xFFF1F0F7),
    surface = Color(0xFF232336),
    onSurface = Color(0xFFF1F0F7),
    card = Color(0xFF2E2E48),
    onCard = Color(0xFFF1F0F7),
    surfaceVariant = Color(0xFF2E2E48),
    onSurfaceVariant = Color(0xFFA0A4B8),
    border = Color(0xFF35354D),
    textPrimary = Color(0xFFF1F0F7),
    textSecondary = Color(0xFFA0A4B8),
    textDisabled = Color(0xFF6B7280),
    success = Color(0xFF10B981),
    onSuccess = Color(0xFF062E21),
    successContainer = Color(0xFF203B42),
    onSuccessContainer = Color(0xFF6EE7B7),
    error = Color(0xFFF87171),
    onError = Color(0xFF330B0B),
    errorContainer = Color(0xFF452F3F),
    onErrorContainer = Color(0xFFFCA5A5),
    bookmark = Color(0xFFF59E0B),
    info = Color(0xFF38BDF8),
    warning = Color(0xFFFBBF24),
    scrim = Color(0xCC000000)
)

// ── تم روشن: همان خانواده‌ی بنفش، با سطوح روشن و متن تیره‌ی باکنتراست ──────────
fun lightPdPalette() = PdPalette(
    isDark = false,
    background = Color(0xFFF4F4FA),
    onBackground = Color(0xFF1B1B2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B2E),
    card = Color(0xFFFFFFFF),
    onCard = Color(0xFF1B1B2E),
    surfaceVariant = Color(0xFFEDEDF6),
    onSurfaceVariant = Color(0xFF5B5F73),
    border = Color(0xFFE3E3EF),
    textPrimary = Color(0xFF1B1B2E),
    textSecondary = Color(0xFF5B5F73),
    textDisabled = Color(0xFF9AA0AE),
    success = Color(0xFF059669),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD1FAE5),
    onSuccessContainer = Color(0xFF065F46),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    bookmark = Color(0xFFD97706),
    info = Color(0xFF0284C7),
    warning = Color(0xFFB45309),
    scrim = Color(0x66000000)
)

/** پالت ساختاری تم جاری. همین را به‌جای رنگ ثابت در صفحه‌ها بخوانید. */
val LocalPdPalette = staticCompositionLocalOf { darkPdPalette() }

/** دسترسی کوتاه به پالت بدون نیاز به `LocalPdPalette.current`. */
object Pd {
    val palette: PdPalette
        @Composable @ReadOnlyComposable
        get() = LocalPdPalette.current

    val background: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.background
    val surface: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.surface
    val card: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.card
    val border: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.border
    val textPrimary: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.textPrimary
    val textSecondary: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.textSecondary
    val textDisabled: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.textDisabled
    val success: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.success
    val error: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.error
    val bookmark: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.bookmark
    val info: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.info
    val warning: Color @Composable @ReadOnlyComposable get() = LocalPdPalette.current.warning
}

// ─────────────────────────────────────────────────────────────────────────────
// accent — رنگ برند. تم روشن از پنهای پررنگ (600) و تم تیره از پنهای روشن (400)
// استفاده می‌کند تا کنتراست روی هر دو پس‌زمینه حفظ شود.
// ─────────────────────────────────────────────────────────────────────────────

data class AccentTone(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color
)

data class ThemeAccent(
    val id: String,
    val labelFa: String,
    val light: AccentTone,
    val dark: AccentTone
)

private fun accent(
    id: String,
    labelFa: String,
    s50: Long,
    s400: Long,
    s600: Long,
    s800: Long,
    s950: Long
): ThemeAccent = ThemeAccent(
    id = id,
    labelFa = labelFa,
    light = AccentTone(
        primary = Color(s600),
        onPrimary = Color.White,
        container = Color(s50),
        onContainer = Color(s800)
    ),
    dark = AccentTone(
        primary = Color(s400),
        onPrimary = Color(s950),
        container = Color(s950),
        onContainer = Color(s50)
    )
)

val DefaultAccentId = "midnight"

val ThemeAccents: List<ThemeAccent> = listOf(
    // «میدنایت رز / بنفش» — پالت پیش‌فرض سامانه، دقیقاً #6C5CE7 / #8B7CF6
    accent("midnight", "میدنایت بنفش", 0xFFedebfc, 0xFF8b7cf6, 0xFF6c5ce7, 0xFF4a3fb8, 0xFF1b1b2e),
    accent("rose", "رز", 0xFFfff1f2, 0xFFfb7185, 0xFFe11d48, 0xFF9f1239, 0xFF4c0519),
    accent("red", "قرمز", 0xFFfef2f2, 0xFFf87171, 0xFFdc2626, 0xFF991b1b, 0xFF450a0a),
    accent("orange", "نارنجی", 0xFFfff7ed, 0xFFfb923c, 0xFFea580c, 0xFF9a3412, 0xFF431407),
    accent("amber", "کهربایی", 0xFFfffbeb, 0xFFfbbf24, 0xFFd97706, 0xFF92400e, 0xFF451a03),
    accent("yellow", "زرد", 0xFFfefce8, 0xFFfacc15, 0xFFca8a04, 0xFF854d0e, 0xFF422006),
    accent("lime", "لیمویی", 0xFFf7fee7, 0xFFa3e635, 0xFF65a30d, 0xFF3f6212, 0xFF1a2e05),
    accent("green", "سبز", 0xFFf0fdf4, 0xFF4ade80, 0xFF16a34a, 0xFF166534, 0xFF052e16),
    accent("emerald", "زمردی", 0xFFecfdf5, 0xFF34d399, 0xFF059669, 0xFF065f46, 0xFF022c22),
    accent("teal", "فیروزه‌ای", 0xFFf0fdfa, 0xFF2dd4bf, 0xFF0d9488, 0xFF115e59, 0xFF042f2e),
    accent("cyan", "آبی فیروزه‌ای", 0xFFecfeff, 0xFF22d3ee, 0xFF0891b2, 0xFF155e75, 0xFF083344),
    accent("sky", "آبی آسمانی", 0xFFf0f9ff, 0xFF38bdf8, 0xFF0284c7, 0xFF075985, 0xFF082f49),
    accent("blue", "آبی", 0xFFeff6ff, 0xFF60a5fa, 0xFF2563eb, 0xFF1e40af, 0xFF172554),
    accent("indigo", "نیلی", 0xFFeef2ff, 0xFF818cf8, 0xFF4f46e5, 0xFF3730a3, 0xFF1e1b4b),
    accent("violet", "بنفش", 0xFFf5f3ff, 0xFFa78bfa, 0xFF7c3aed, 0xFF5b21b6, 0xFF2e1065),
    accent("purple", "ارغوانی", 0xFFfaf5ff, 0xFFc084fc, 0xFF9333ea, 0xFF6b21a8, 0xFF3b0764),
    accent("fuchsia", "سرخابی", 0xFFfdf4ff, 0xFFe879f9, 0xFFc026d3, 0xFF86198f, 0xFF4a044e),
    accent("pink", "صورتی", 0xFFfdf2f8, 0xFFf472b6, 0xFFdb2777, 0xFF9d174d, 0xFF500724)
)

fun accentById(id: String?): ThemeAccent =
    ThemeAccents.firstOrNull { it.id == id } ?: ThemeAccents.first()

/**
 * تنال همین accent برای تم جاری.
 *
 * نمونه‌ی رنگ در صفحه‌ی تنظیمات باید همان تنالی را نشان دهد که کاربر دیدنی است؛
 * در تم تیره پنه‌ی روشن (400) و در تم روشن پنه‌ی پررنگ (600) استفاده می‌شود.
 */
val ThemeAccent.tone: AccentTone
    @Composable @ReadOnlyComposable
    get() = if (LocalPdPalette.current.isDark) dark else light

// ─────────────────────────────────────────────────────────────────────────────
// رنگ‌های معنادار (semantic)
// ─────────────────────────────────────────────────────────────────────────────

/** نشان «یادگرفته‌شده». */
val EmeraldLearned: Color
    @Composable @ReadOnlyComposable
    get() = LocalPdPalette.current.success

/** نشان «بوکمارک». */
val AmberBookmark: Color
    @Composable @ReadOnlyComposable
    get() = LocalPdPalette.current.bookmark

/** رنگ ثابت پس‌زمینه‌ی سبز برای حالت‌هایی که تم را دنبال نمی‌کنند. */
val EmeraldLearnedDark = Color(0xFF34D399)

/** رنگ معنادار «موفقیت/اتصال برقرار» در تم روشن. */
val EmeraldLearnedLight = Color(0xFF059669)

/** آبی پزشکی سامانه‌ی وب — برای نشان «در حال بارگذاری» و لینک‌های سرد. */
val MedicalCyan = Color(0xFF0284C7)
val MedicalCyanDark = Color(0xFF38BDF8)
val MedicalCyanContainerLight = Color(0xFFE0F2FE)
val MedicalCyanContainerDark = Color(0xFF082F49)

val EmeraldContainerLight = Color(0xFFD1FAE5)
val EmeraldContainerDark = Color(0xFF022C22)
val AmberContainerLight = Color(0xFFFEF3C7)
val AmberContainerDark = Color(0xFF451A03)

// ─────────────────────────────────────────────────────────────────────────────
// رنگ برند جاری
//
// نام historical «CrimsonPrimary» حفظ شده تا ده‌ها استفاده‌ی موجود بدون بازنویسی
// کار کنند، اما مقدار آن حالا از MaterialTheme خوانده می‌شود و با انتخاب کاربر
// و با تم روشن/تیره عوض می‌شود. برای دکمه‌ها، غلظت‌ها و حالت‌های انتخاب‌شده
// از همین استفاده کنید تا برند در کل برنامه یکدست بماند.
// ─────────────────────────────────────────────────────────────────────────────
val CrimsonPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

/** رنگ برند برای زمینه‌های روشن (accent در تم روشن). */
val CrimsonAccent: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondary
