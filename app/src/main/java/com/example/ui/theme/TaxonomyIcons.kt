package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * هم‌نمایی آیکون‌های سامانه‌ی وب با آیکون‌های Material.
 *
 * سامانه‌ی وب برای مسیرهای انتقال و میزبان‌ها آیکون Font Awesome می‌فرستد
 * (`icon: "fa-handshake-simple"` و ...). آن نام‌ها را به‌صورت یک‌به‌یک و با همان
 * معنا به آیکون Material نگاشت می‌کنیم تا برنامه همان نمادهای بصری وب را نشان دهد
 * و آیکون نامربوط یا دلخواه استفاده نشود.
 *
 * برخی آیکون‌های حیوانی Font Awesome (اسب، شتر، کرگدمان و ...) معادل Material ندارد؛
 * برای آن‌ها نزدیک‌ترین خانواده‌ی معنایی (Pets / Agriculture / PestControl / Nature)
 * برگزیده شده است.
 */
object TaxonomyIcons {

    /** آیکون مسیر انتقال از نام fa-* که وب می‌فرستد. */
    fun transmission(slugOrIcon: String?): ImageVector {
        val key = (slugOrIcon ?: "").removePrefix("fa-").substringBefore("-")
        return when (key) {
            "handshake" -> Icons.Default.Handshake
            "droplet" -> Icons.Default.WaterDrop
            "wind" -> Icons.Default.Air
            "restroom" -> Icons.Default.Wc
            "utensils" -> Icons.Default.Restaurant
            "water" -> Icons.Default.Water
            "mosquito" -> Icons.Default.PestControl
            "spider" -> Icons.Default.BugReport
            "paw" -> Icons.Default.Pets
            "syringe" -> Icons.Default.Vaccines
            "heart-crack", "heart" -> Icons.Default.HeartBroken
            "baby" -> Icons.Default.ChildCare
            "user-doctor", "user" -> Icons.Default.MedicalServices
            "seedling" -> Icons.Default.Yard
            "hospital" -> Icons.Default.LocalHospital
            "microphone-lines", "microphone" -> Icons.Default.Mic
            else -> byTransmissionSlug(slugOrIcon)
        }
    }

    /** نگاشت جایزین بر پایه‌ی اسلاگ (اگر وب اسلاگ بفرستد نه نام آیکون). */
    private fun byTransmissionSlug(slug: String?): ImageVector = when (slug) {
        "direct_contact" -> Icons.Default.Handshake
        "droplet" -> Icons.Default.WaterDrop
        "airborne" -> Icons.Default.Air
        "fecal_oral" -> Icons.Default.Wc
        "foodborne" -> Icons.Default.Restaurant
        "waterborne" -> Icons.Default.Water
        "vector_insect" -> Icons.Default.PestControl
        "vector_tick" -> Icons.Default.BugReport
        "animal_contact" -> Icons.Default.Pets
        "bloodborne" -> Icons.Default.Bloodtype
        "sexual" -> Icons.Default.HeartBroken
        "vertical" -> Icons.Default.ChildCare
        "iatrogenic" -> Icons.Default.MedicalServices
        "environmental" -> Icons.Default.Yard
        "nosocomial" -> Icons.Default.LocalHospital
        "aerosol_procedure" -> Icons.Default.Mic
        else -> Icons.Default.Coronavirus
    }

    /** آیکون گروه میزبان از نام fa-* که وب می‌فرستد. */
    fun host(slugOrIcon: String?): ImageVector {
        val key = (slugOrIcon ?: "").removePrefix("fa-").substringBefore("-")
        return when (key) {
            "user" -> Icons.Default.Person
            "piggy-bank", "pig" -> Icons.Default.Pets
            "cow" -> Icons.Default.Agriculture
            "paw" -> Icons.Default.Pets
            "horse" -> Icons.Default.Pets
            "hippo" -> Icons.Default.Pets
            "dog" -> Icons.Default.Pets
            "cat" -> Icons.Default.Pets
            "mouse" -> Icons.Default.Mouse
            "otter" -> Icons.Default.CrueltyFree
            "dragon" -> Icons.Default.Pets
            "crow" -> Icons.Default.FlutterDash
            "dove" -> Icons.Default.Flight
            "tree" -> Icons.Default.Park
            "mosquito" -> Icons.Default.PestControl
            "spider" -> Icons.Default.BugReport
            "bug" -> Icons.Default.BugReport
            "cuttlefish" -> Icons.Default.Phishing
            "fish" -> Icons.Default.Phishing
            "leaf" -> Icons.Default.Eco
            "seedling" -> Icons.Default.Spa
            else -> byHostSlug(slugOrIcon)
        }
    }

    private fun byHostSlug(slug: String?): ImageVector = when (slug) {
        "human" -> Icons.Default.Person
        "swine" -> Icons.Default.Pets
        "cattle" -> Icons.Default.Agriculture
        "sheep_goat" -> Icons.Default.Agriculture
        "horse" -> Icons.Default.Pets
        "camel" -> Icons.Default.Pets
        "dog" -> Icons.Default.Pets
        "cat" -> Icons.Default.Pets
        "rodent" -> Icons.Default.Mouse
        "lagomorph" -> Icons.Default.CrueltyFree
        "bat" -> Icons.Default.Nature
        "poultry" -> Icons.Default.FlutterDash
        "wild_bird" -> Icons.Default.Flight
        "wildlife" -> Icons.Default.Nature
        "mosquito" -> Icons.Default.PestControl
        "tick" -> Icons.Default.BugReport
        "flea" -> Icons.Default.BugReport
        "snail" -> Icons.Default.Hive
        "aquatic" -> Icons.Default.Phishing
        "plant" -> Icons.Default.Eco
        "fungus_host" -> Icons.Default.Spa
        else -> Icons.Default.Pets
    }

    /** آیکون بخش‌های بالینی پرونده در دossierSheet تعریف شده؛ اینجا نگاشت آیکون‌های
     *  انتقال و میزبان که وب می‌فرستد نگه داشته می‌شود. */
}

/**
 * رنگ سطح مسیر انتقال — هم‌رنگ با TX_LEVEL_CLASS سامانه‌ی وب.
 *
 * وب: primary=rose-600، secondary=amber-500، rare=slate-400.
 * در پالت این برنامه rose به‌جای رنگ خطا برای «مسیر اصلی» استفاده می‌شود تا
 * خوانایی و معنای وب حفظ شود.
 */
@Composable
fun transmissionLevelColor(level: String?): Color = when (level) {
    "primary" -> CrimsonPrimary
    "secondary" -> Pd.warning
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * رنگ نقش میزبان — هم‌رنگ با HOST_ROLE_CLASS سامانه‌ی وب.
 *
 * وب: host=rose-500، reservoir=violet-500، vector=teal-500، accidental=sky-500،
 * dead_end=slate-500.
 */
@Composable
fun hostRoleColor(role: String?): Color = when (role) {
    "host" -> MaterialTheme.colorScheme.error
    "reservoir" -> MaterialTheme.colorScheme.primary
    "vector" -> MaterialTheme.colorScheme.tertiary
    "accidental" -> Pd.info
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** آیکون پیش‌فرض بخش بالینی که انجمن وب می‌شناسد. */
val DefaultSectionIcon: ImageVector get() = Icons.Default.MonitorHeart
