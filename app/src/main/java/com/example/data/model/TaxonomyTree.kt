package com.example.data.model

import java.util.Locale

/**
 * ساختار درخت تاکسونومی — دقیقاً مثل صفحه‌ی تاکسونومی سامانه‌ی وب.
 *
 * درخت از روی داده‌ی واقعی ساخته می‌شود: دسته › رده › خانواده › پاتوژن.
 * هیچ فهرست ثابتی ندارد و با افزایش پایگاه‌داده فقط تعداد گره‌ها بیشتر می‌شود.
 *
 * این فایل عمداً بدون وابستگی به Compose است تا منطق گروه‌بندی، مرتب‌سازی،
 * جستجو و صاف‌کردن درخت در آزمون واحد قابل‌آزمودن باشد.
 */

/** گره‌ی خانواده داخل یک رده. */
data class TaxFamilyNode(
    val key: String,
    val titleFa: String,
    val titleEn: String?,
    val diseases: List<Disease>
)

/** گره‌ی رده (Class) داخل یک دسته. */
data class TaxClassNode(
    val key: String,
    val titleFa: String,
    val titleEn: String?,
    val families: List<TaxFamilyNode>,
    val diseases: List<Disease>
)

/** گره‌ی دسته (Category) — ریشه‌ی درخت. */
data class TaxCategoryNode(
    val key: String,
    val title: String,
    val classes: List<TaxClassNode>,
    val diseases: List<Disease>
)

/** یک ردیف صاف‌شده‌ی درخت، با عمق و نوع گره. */
sealed interface TaxEntry {
    val stableKey: String
    val depth: Int
    val nodeKey: String

    data class Category(val node: TaxCategoryNode, override val depth: Int = 0) : TaxEntry {
        override val stableKey: String get() = "cat:${node.key}"
        override val nodeKey: String get() = node.key
    }

    data class Class(val node: TaxClassNode, override val depth: Int = 1) : TaxEntry {
        override val stableKey: String get() = "cls:${node.key}"
        override val nodeKey: String get() = node.key
    }

    data class Family(val node: TaxFamilyNode, override val depth: Int = 2) : TaxEntry {
        override val stableKey: String get() = "fam:${node.key}"
        override val nodeKey: String get() = node.key
    }

    data class Pathogen(val disease: Disease, override val depth: Int = 3) : TaxEntry {
        override val stableKey: String get() = "dis:${disease.id}"
        override val nodeKey: String get() = "dis:${disease.id}"
    }
}

/**
 * ساخت درخت از فهرست پاتوژن‌ها.
 *
 * با جستجو فقط پاتوژن‌های مطابق در درخت می‌آیند (مثل فیلتر درخت در وب) و
 * گروه‌های خالی ساخته نمی‌شوند.
 */
fun buildTaxonomyTree(all: List<Disease>, query: String): List<TaxCategoryNode> {
    val filtered = if (query.isBlank()) all else all.filter { matchesQuery(it, query) }

    return filtered
        .groupBy { it.category.trim().ifBlank { "سایر" } }
        .map { (category, categoryDiseases) ->
            val classes = categoryDiseases
                .groupBy { classKey(it.class_fa, it.class_en) }
                .map { (clsKey, clsDiseases) ->
                    val families = clsDiseases
                        .groupBy { familyKey(it.family_fa, it.family_en) }
                        .map { (famKey, famDiseases) ->
                            TaxFamilyNode(
                                key = famKey,
                                titleFa = famDiseases.firstOrNull()?.family_fa?.trim().orEmpty()
                                    .ifBlank { famDiseases.firstOrNull()?.family_en?.trim().orEmpty() }
                                    .ifBlank { "خانواده عمومی" },
                                titleEn = famDiseases.firstOrNull()
                                    ?.family_en?.trim()?.takeIf { it.isNotBlank() },
                                diseases = famDiseases.sortedBy { it.id }
                            )
                        }
                        .sortedWith(compareBy({ it.titleFa }, { it.titleEn ?: "" }))

                    TaxClassNode(
                        key = clsKey,
                        // عنوان هم مثل وب نام خالص رده است، بدون توضیح «(سرده ...)»
                        titleFa = cleanClassName(clsDiseases.firstOrNull()?.class_fa)
                            .ifBlank { cleanClassName(clsDiseases.firstOrNull()?.class_en) }
                            .ifBlank { "رده عمومی" },
                        titleEn = clsDiseases.firstOrNull()
                            ?.class_en?.trim()?.takeIf { it.isNotBlank() },
                        families = families,
                        diseases = clsDiseases
                    )
                }
                .sortedWith(compareBy({ it.titleFa }, { it.titleEn ?: "" }))

            TaxCategoryNode(
                key = category,
                title = category,
                classes = classes,
                diseases = categoryDiseases
            )
        }
        .sortedWith(compareByDescending<TaxCategoryNode> { it.diseases.size }.thenBy { it.title })
}

/**
 * صاف کردن درخت به ردیف‌های قابل‌نمایش بر اساس وضعیت باز/بسته‌بودن شاخه‌ها.
 *
 * فقط شاخه‌های باز پاتوژن خود را برمی‌گردانند، پس با صدها پاتوژن و شاخه‌های
 * بسته تعداد ردیف‌ها کم و پیمایش نرم می‌ماند.
 */
fun flatten(
    tree: List<TaxCategoryNode>,
    expanded: Map<String, Boolean>
): List<TaxEntry> {
    val out = mutableListOf<TaxEntry>()
    tree.forEach { cat ->
        out += TaxEntry.Category(cat)
        if (expanded[cat.key] != true) return@forEach
        cat.classes.forEach { cls ->
            out += TaxEntry.Class(cls)
            if (expanded[cls.key] != true) return@forEach
            cls.families.forEach { fam ->
                out += TaxEntry.Family(fam)
                if (expanded[fam.key] != true) return@forEach
                fam.diseases.forEach { out += TaxEntry.Pathogen(it) }
            }
        }
    }
    return out
}

/** یکدست‌سازی نام فارسی/انگلیسی برای ادغام رده‌های یکسان (ی/ي، ک/ك، نیم‌فاصله). */
fun normalizeTaxonomyKey(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return value
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace(Regex("[\\u200c\\s\\-_]+"), "")
        .lowercase(Locale.ROOT)
        .trim()
}

/** کلید رده — نام خالص بدون توضیح «(سرده ...)» داخل پرانتز. */
fun classKey(fa: String?, en: String?): String {
    val cleaned = cleanClassName(fa ?: en)
    return normalizeTaxonomyKey(cleaned).ifBlank { "general_class" }
}

/** کلید خانواده. */
fun familyKey(fa: String?, en: String?): String {
    return normalizeTaxonomyKey((fa?.trim()?.takeIf { it.isNotBlank() }) ?: en?.trim())
        .ifBlank { "general_family" }
}

/** نام خالص رده را از توضیح «(سرده ...)» داخل پرانتز پاک می‌کند — مثل وب. */
fun cleanClassName(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return raw
        .replace(
            Regex("\\s*[\\(\\[（][^\\)\\]）]*(?:سرده|جنس|genus)[^\\)\\]）]*[\\)\\]）]", RegexOption.IGNORE_CASE),
            ""
        )
        .replace(
            Regex("\\s*(?:[-–—،,|/]\\s*)?(?:سرده|جنس|genus)\\s*[:：]?\\s.*$", RegexOption.IGNORE_CASE),
            ""
        )
        .replace(Regex("\\s+"), " ")
        .trim()
}

/** تطبیق جستجو روی همه‌ی نام‌های تاکسونومیک و نام پاتوژن — مثل وب. */
fun matchesQuery(d: Disease, query: String): Boolean {
    val q = normalizeTaxonomyKey(query)
    if (q.isBlank()) return true
    val pool = listOf(
        d.category, d.class_fa, d.class_en, d.family_fa, d.family_en,
        d.genus_fa, d.genus_en, d.name_fa, d.name_en, d.agent_fa, d.agent_en
    ).joinToString(" ") { normalizeTaxonomyKey(it) }
    return pool.contains(q)
}
