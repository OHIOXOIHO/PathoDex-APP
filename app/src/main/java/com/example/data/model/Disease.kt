package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * یک بخش بالینی از پرونده‌ی مرض (همه‌گیرشناسی، علائم، پاتوژنز، تشخیص، درمان،
 * پیشگیری، پیش‌آگهی). سامانه‌ی وب این بخش‌ها را از مسیر
 * GET /api/diseases/{id} برمی‌گرداند.
 */
@JsonClass(generateAdapter = true)
data class DiseaseSection(
    @Json(name = "key") val key: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "verified") val verified: Boolean? = false
)

/** مسیر انتقال عامل بیماری‌زا (تماس مستقیم، قطره‌ای، هوایی و ...). */
@JsonClass(generateAdapter = true)
data class TransmissionRoute(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "level") val level: String? = null,
    @Json(name = "level_fa") val level_fa: String? = null,
    @Json(name = "notes") val notes: String? = null
)

/** میزبان عامل بیماری‌زا (انسان، حیوان، ناقل و ...). */
@JsonClass(generateAdapter = true)
data class HostGroup(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "role_fa") val role_fa: String? = null,
    @Json(name = "species_note") val species_note: String? = null
)

@JsonClass(generateAdapter = true)
data class Disease(
    @Json(name = "id") val id: Int,
    @Json(name = "category") val category: String,
    @Json(name = "name_fa") val name_fa: String,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "agent_fa") val agent_fa: String,
    @Json(name = "agent_en") val agent_en: String? = null,
    @Json(name = "description_fa") val description_fa: String,
    @Json(name = "family_fa") val family_fa: String? = null,
    @Json(name = "family_en") val family_en: String? = null,
    @Json(name = "class_fa") val class_fa: String? = null,
    @Json(name = "class_en") val class_en: String? = null,
    @Json(name = "genus_fa") val genus_fa: String? = null,
    @Json(name = "genus_en") val genus_en: String? = null,
    @Json(name = "slug") val slug: String? = null,
    /** مسیر کامل و کانونیک این پرونده در سامانه‌ی وب، مثل /disease/1-عفونت-... */
    @Json(name = "url_path") val url_path: String? = null,
    /** مسیر کانونیک صفحه‌ی دسته‌ی این پاتوژن، مثل /category/باکتریایی */
    @Json(name = "category_url_path") val category_url_path: String? = null,
    @Json(name = "category_slug") val category_slug: String? = null,
    @Json(name = "host_scope") val host_scope: String? = null,
    @Json(name = "created_at") val created_at: String? = null,
    @Json(name = "updated_at") val updated_at: String? = null,
    @Json(name = "images") val images: List<DiseaseImage>? = emptyList(),
    @Json(name = "sections") val sections: List<DiseaseSection> = emptyList(),
    @Json(name = "transmission") val transmission: List<TransmissionRoute> = emptyList(),
    @Json(name = "hosts") val hosts: List<HostGroup> = emptyList(),
    @Json(name = "is_favorite") val is_favorite: Boolean? = false,
    @Json(name = "is_bookmarked") val is_bookmarked: Boolean? = false,
    @Json(name = "is_learned") val is_learned: Boolean? = false,
    @Json(name = "personal_note") val personal_note: String? = ""
) {
    /** نشانی کانونیک صفحه‌ی این پرونده روی سامانه‌ی وب. */
    val canonicalUrl: String
        get() = SiteUrls.absolute(url_path?.trim().takeIf { !it.isNullOrBlank() } ?: "/disease/$id")

    /** نشانی کانونیک صفحه‌ی دسته‌ی این پاتوژن روی سامانه‌ی وب. */
    val categoryUrl: String
        get() = SiteUrls.absolute(
            category_url_path?.trim().takeIf { !it.isNullOrBlank() }
                ?: "/category/${category_slug?.takeIf { it.isNotBlank() } ?: category}"
        )

    /** برچسب خوانای محدوده‌ی میزبان، مشابه سامانه‌ی وب. */
    val hostScopeLabel: String
        get() = when (host_scope) {
            "human" -> "پاتوژن انسانی"
            "non_human" -> "پاتوژن غیرانسانی"
            "both" -> "انسانی و غیرانسانی"
            else -> "—"
        }

    /**
     * پاتوژن‌های مرتبط بر اساس روابط تاکسونومی واقعی — دقیقاً منطق سامانه‌ی وب.
     *
     * اولویت از نزدیک‌ترین رتبه به دورترین:
     * هم‌گونه (agent) › هم‌سرده › هم‌خانواده › هم‌رده › هم‌دسته.
     *
     * چون داده‌ی وب فیلد «order» و «species» جداگانه ندارد، نزدیک‌ترین رتبه‌ی
     * موجود هم‌سرده است و «هم‌گونه» از برابری نام علمی عامل به دست می‌آید.
     * امتیاز هر رتبه یک عدد متمایز است تا مرتب‌سازی پایدار بماند و با ترتیب
     * پایگاه‌داده گره نخورد.
     *
     * @return فهرستی از پاتوژن و برچسب فارسی رابطه‌ی آن، نزدیک‌ترین در ابتدا.
     */
    fun relatedPathogens(all: List<Disease>, limit: Int = 3): List<RelatedPathogen> {
        val self = this
        return all.asSequence()
            .filter { it.id != self.id }
            .mapNotNull { other ->
                val score = taxonomicScore(self, other) ?: return@mapNotNull null
                RelatedPathogen(other, score.second, score.first)
            }
            .sortedWith(compareByDescending<RelatedPathogen> { it.score }.thenBy { it.disease.id })
            .take(limit)
            .toList()
    }

    private fun taxonomicScore(a: Disease, b: Disease): Pair<Int, String>? {
        // هم‌گونه: نام علمی عامل یکی است (وقتی سرده هم یکی باشد رشته‌ی طولانی‌تر را می‌گیریم)
        val agentA = a.agent_en?.trim()?.lowercase()
        val agentB = b.agent_en?.trim()?.lowercase()
        if (!agentA.isNullOrBlank() && agentA == agentB) return 60 to "هم‌گونه"

        val genusA = a.genus_en?.trim()?.lowercase() ?: a.genus_fa?.trim()?.lowercase()
        val genusB = b.genus_en?.trim()?.lowercase() ?: b.genus_fa?.trim()?.lowercase()
        if (!genusA.isNullOrBlank() && genusA == genusB) return 50 to "هم‌سرده"

        val famA = a.family_en?.trim()?.lowercase() ?: a.family_fa?.trim()?.lowercase()
        val famB = b.family_en?.trim()?.lowercase() ?: b.family_fa?.trim()?.lowercase()
        if (!famA.isNullOrBlank() && famA == famB) return 40 to "هم‌خانواده"

        val clsA = cleanClass(a.class_fa ?: a.class_en)
        val clsB = cleanClass(b.class_fa ?: b.class_en)
        if (clsA.isNotEmpty() && clsA == clsB) return 30 to "هم‌رده"

        if (a.category.isNotBlank() && a.category == b.category) return 20 to "هم‌دسته"

        return null
    }

    /** نام خالص رده را از توضیح «(سرده ...)» داخل پرانتز پاک می‌کند — مثل وب. */
    private fun cleanClass(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.replace(Regex("\\s*[\\(\\[（][^\\)\\]）]*(?:سرده|جنس|genus)[^\\)\\]）]*[\\)\\]）]", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*(?:[-–—،,|/]\\s*)?(?:سرده|جنس|genus)\\s*[:：]?\\s.*$", RegexOption.IGNORE_CASE), "")
            .trim()
            .lowercase()
    }

    /** بخش‌هایی که متن واقعی دارند؛ برای نمایش در پرونده. */
    val populatedSections: List<DiseaseSection>
        get() = sections.filter { !it.body.isNullOrBlank() }

    /**
     * آیا پرونده‌ی کامل (با بخش‌های بالینی) در دستگاه موجود است؟
     * فهرست اصلی فقط description_fa را می‌فرستد؛ بخش‌ها از مسیر جزئیات می‌آیند.
     */
    val hasFullSections: Boolean
        get() = populatedSections.isNotEmpty()

    /** مسیرهای انتقال با برچسب فارسیِ سطح (اصلی/فرعی). */
    val transmissionLabeled: List<Pair<TransmissionRoute, String>>
        get() = transmission.map { route ->
            route to when (route.level) {
                "primary" -> "اصلی"
                "secondary" -> "فرعی"
                else -> route.level_fa ?: route.level ?: ""
            }
        }
}

/** پاتوژن مرتبط با برچسب رابطه‌ی تاکسونومی و امتیاز نزدیکی. */
data class RelatedPathogen(
    val disease: Disease,
    val label: String,
    val score: Int
)

/**
 * سامانه‌ی وب PathoDex — منبع نشانی‌های کانونیک و پیوندهای عمیق.
 *
 * هم برای اشتراک‌گذاری (ACTION_SEND) و هم برای پیوندهای عمیق (App Links) به
 * «نشانی وب همان محتوا» نیاز داریم؛ ساختن نشانی از روی نام نمایشی ممنوع است.
 */
object SiteUrls {
    const val HOST = "https://pathodex.ir"

    /** مسیر نسبی را به نشانی کامل تبدیل می‌کند؛ نشانی کامل را دست‌نخورده می‌گذارد. */
    fun absolute(pathOrUrl: String): String = when {
        pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://") -> pathOrUrl
        pathOrUrl.startsWith("/") -> "$HOST$pathOrUrl"
        else -> "$HOST/$pathOrUrl"
    }
}

@JsonClass(generateAdapter = true)
data class DiseaseImage(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "type") val type: String = "general", // pathogen, symptom, general
    @Json(name = "url") val url: String
) {
    /**
     * Resolves the full URL according to the critical backend rule:
     * Relative paths starting with /uploads/ or / must be prefixed with https://pathodex.ir
     */
    val resolvedUrl: String
        get() = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "https://pathodex.ir$url"
            else -> "https://pathodex.ir/$url"
        }
}
