package com.example

import com.example.data.model.Disease
import com.example.data.model.SiteUrls
import com.example.data.model.TaxEntry
import com.example.data.model.buildTaxonomyTree
import com.example.data.model.classKey
import com.example.data.model.cleanClassName
import com.example.data.model.familyKey
import com.example.data.model.flatten
import com.example.data.model.matchesQuery
import com.example.data.model.normalizeTaxonomyKey
import com.example.util.DeepLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * آزمون‌های منطق افزوده‌شده: نشانی کانونیک، پاتوژن‌های نزدیک تاکسونومی، درخت
 * تاکسونومی و پیوندهای عمیق.
 *
 * این منطق دقیقاً همان چیزی است که برای «قابل‌اعتماد بودن» داده‌ها خواسته شده،
 * پس با آزمون پوشانده می‌شود تا بعداً بی‌صدا عوض نشود.
 *
 * منطق مسیریابی پیوند عمداً در یک تابع خالص [DeepLinks.parse] بدون وابستگی به
 * android.net.Uri نگه داشته شده تا همین‌جا قابل‌آزمودن باشد؛ نسخه‌ی Uri در
 * [DeepLinksUriTest] با Robolectric پوشش داده شده است.
 */
class PathologyParityTest {

    private fun disease(
        id: Int,
        nameFa: String? = null,
        genus: String? = null,
        genusEn: String? = null,
        family: String? = null,
        familyEn: String? = null,
        klass: String? = null,
        classEn: String? = null,
        category: String = "باکتری‌ها",
        agentEn: String? = null,
        urlPath: String? = null,
        categoryUrlPath: String? = null,
        categorySlug: String? = null
    ) = Disease(
        id = id,
        category = category,
        name_fa = nameFa ?: "پاتوژن $id",
        name_en = "Pathogen $id",
        agent_fa = "عامل $id",
        agent_en = agentEn,
        description_fa = "توضیح $id",
        family_fa = family,
        family_en = familyEn,
        class_fa = klass,
        class_en = classEn ?: klass,
        genus_fa = genus,
        genus_en = genusEn,
        url_path = urlPath,
        category_url_path = categoryUrlPath,
        category_slug = categorySlug
    )

    // ── نشانی کانونیک (اشتراک‌گذاری) ─────────────────────────────────────────

    @Test
    fun `canonical url comes from the website url_path`() {
        val d = disease(id = 1, urlPath = "/disease/1-عفونت-staphylococcal-infections")
        assertEquals(
            "https://pathodex.ir/disease/1-عفونت-staphylococcal-infections",
            d.canonicalUrl
        )
    }

    @Test
    fun `canonical url keeps an already absolute url untouched`() {
        val d = disease(id = 7, urlPath = "https://pathodex.ir/disease/7-x")
        assertEquals("https://pathodex.ir/disease/7-x", d.canonicalUrl)
    }

    @Test
    fun `canonical url never duplicates the domain`() {
        val d = disease(id = 7, urlPath = "https://pathodex.ir/disease/7-x")
        assertEquals(1, Regex("pathodex\\.ir").findAll(d.canonicalUrl).count())
    }

    @Test
    fun `canonical url falls back to the numeric id when url_path is missing`() {
        assertEquals("https://pathodex.ir/disease/42", disease(id = 42).canonicalUrl)
    }

    @Test
    fun `canonical url falls back when url_path is blank`() {
        assertEquals("https://pathodex.ir/disease/42", disease(id = 42, urlPath = "   ").canonicalUrl)
    }

    @Test
    fun `canonical url keeps the whole path and slug`() {
        val slug = "/disease/99-%D8%B9%D9%81%D9%88%D9%86%D8%AA-%D9%87%D8%A7%DB%8C-%D8%A7%D8%B3%D8%AA%D8%A7%D9%81%DB%8C%D9%84%D9%88%DA%A9%D9%88%DA%A9%DB%8C"
        assertEquals("https://pathodex.ir" + slug, disease(id = 99, urlPath = slug).canonicalUrl)
    }

    @Test
    fun `category url keeps an absolute website path`() {
        val d = disease(id = 1, categoryUrlPath = "https://pathodex.ir/category/باکتریایی")
        assertEquals("https://pathodex.ir/category/باکتریایی", d.categoryUrl)
    }

    @Test
    fun `category url falls back to the category slug`() {
        val d = disease(id = 1, categorySlug = "ویروسی", category = "ویروس‌ها")
        assertEquals("https://pathodex.ir/category/ویروسی", d.categoryUrl)
    }

    @Test
    fun `site urls absolute keeps full urls untouched`() {
        assertEquals("https://example.com/a", SiteUrls.absolute("https://example.com/a"))
        assertEquals("http://example.com/a", SiteUrls.absolute("http://example.com/a"))
        assertEquals("https://pathodex.ir/x", SiteUrls.absolute("/x"))
        assertEquals("https://pathodex.ir/x", SiteUrls.absolute("x"))
    }

    // ── پاتوژن‌های نزدیک تاکسونومی ────────────────────────────────────────────

    @Test
    fun `same genus ranks above same family which ranks above same class`() {
        val anthrax = disease(id = 1, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه", klass = "باسیل")
        val list = listOf(
            anthrax,
            disease(id = 2, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه", klass = "باسیل"),
            disease(id = 3, genus = "کلوستریدیوم", genusEn = "Clostridium", family = "باسیلاسه", klass = "باسیل"),
            disease(id = 4, genus = "استافیلوکوک", genusEn = "Staphylococcus", family = "استافیلوکوکاسه", klass = "باسیل")
        )

        val related = anthrax.relatedPathogens(list)

        assertEquals(3, related.size)
        assertEquals("هم‌سرده", related[0].label)
        assertEquals(2, related[0].disease.id)
        assertEquals("هم‌خانواده", related[1].label)
        assertEquals(3, related[1].disease.id)
        assertEquals("هم‌رده", related[2].label)
        assertEquals(4, related[2].disease.id)
    }

    @Test
    fun `unrelated pathogens are never suggested`() {
        val anthrax = disease(id = 1, genus = "باسیلوس", family = "باسیلاسه", klass = "باسیل", category = "باکتری‌ها")
        val virus = disease(id = 2, genus = "ویروس", family = "ويروسی", klass = "ویروس", category = "ویروسی")

        assertTrue(
            "must not suggest an unrelated pathogen",
            anthrax.relatedPathogens(listOf(anthrax, virus)).isEmpty()
        )
    }

    @Test
    fun `same species beats same genus`() {
        val a = disease(id = 1, genus = "باسیلوس", genusEn = "Bacillus", agentEn = "Bacillus anthracis")
        val b = disease(id = 2, genus = "باسیلوس", genusEn = "Bacillus", agentEn = "Bacillus anthracis")
        val c = disease(id = 3, genus = "باسیلوس", genusEn = "Bacillus", agentEn = "Bacillus cereus")

        val related = a.relatedPathogens(listOf(a, b, c))

        assertEquals("هم‌گونه", related[0].label)
        assertEquals(2, related[0].disease.id)
        assertEquals("هم‌سرده", related[1].label)
    }

    @Test
    fun `ordering is stable and does not depend on database order`() {
        val a = disease(id = 1, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه")
        val list = listOf(
            disease(id = 9, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه"),
            a,
            disease(id = 5, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه")
        )

        assertEquals(listOf(5, 9), a.relatedPathogens(list).map { it.disease.id })
    }

    @Test
    fun `related list is limited to the requested size`() {
        val a = disease(id = 1, genus = "باسیلوس", genusEn = "Bacillus")
        val list = listOf(a) + (2..10).map { disease(id = it, genus = "باسیلوس", genusEn = "Bacillus") }

        assertEquals(3, a.relatedPathogens(list).size)
        assertEquals(2, a.relatedPathogens(list, limit = 2).size)
    }

    @Test
    fun `same category is the weakest relation and still labelled`() {
        val a = disease(id = 1, genus = "A", genusEn = "A", family = "F1", klass = "C1")
        val b = disease(id = 2, genus = "B", genusEn = "B", family = "F2", klass = "C2")

        val related = a.relatedPathogens(listOf(a, b))

        assertEquals(1, related.size)
        assertEquals("هم‌دسته", related[0].label)
    }

    @Test
    fun `farsi genus is compared when english is absent`() {
        val a = disease(id = 1, genus = "باسیلوس", genusEn = null, family = "باسیلاسه")
        val b = disease(id = 2, genus = "باسیلوس", genusEn = null, family = "باسیلاسه")

        assertEquals("هم‌سرده", a.relatedPathogens(listOf(a, b))[0].label)
    }

    // ── درخت تاکسونومی ────────────────────────────────────────────────────────

    @Test
    fun `tree groups by category then class then family`() {
        val list = listOf(
            disease(id = 1, genus = "GA", genusEn = "GA", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA"),
            disease(id = 2, genus = "GB", genusEn = "GB", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA"),
            disease(id = 3, genus = "GC", genusEn = "GC", family = "FB", familyEn = "FB", klass = "CB", classEn = "CB")
        )

        val tree = buildTaxonomyTree(list, "")

        assertEquals(1, tree.size)
        val cat = tree[0]
        assertEquals("باکتری‌ها", cat.title)
        assertEquals(3, cat.diseases.size)
        assertEquals(2, cat.classes.size)
        val classA = cat.classes.first { it.titleFa == "CA" }
        // هر دو پاتوژن در یک خانواده‌اند
        assertEquals(1, classA.families.size)
        assertEquals(listOf(1, 2), classA.families.first { it.titleFa == "FA" }.diseases.map { it.id })
    }

    @Test
    fun `categories are ordered by size then name`() {
        val list = listOf(
            disease(id = 1, category = "کوچک"),
            disease(id = 2, category = "بزرگ"),
            disease(id = 3, category = "بزرگ")
        )

        assertEquals(listOf("بزرگ", "کوچک"), buildTaxonomyTree(list, "").map { it.title })
    }

    @Test
    fun `class names are cleaned before grouping`() {
        val list = listOf(
            disease(id = 1, klass = "باسیل (سرده Bacillus)"),
            disease(id = 2, klass = "باسیل")
        )

        val tree = buildTaxonomyTree(list, "")

        assertEquals(1, tree[0].classes.size)
        assertEquals("باسیل", tree[0].classes[0].titleFa)
        assertEquals(2, tree[0].classes[0].diseases.size)
    }

    @Test
    fun `class key ignores Arabic ye and kaf differences`() {
        assertEquals(classKey("باسیل", null), classKey("باسيل", null))
        assertEquals(classKey("کلستریدیوم", null), classKey("كلستریدیوم", null))
        assertEquals("general_class", classKey(null, null))
    }

    @Test
    fun `family key normalises and falls back to english`() {
        assertEquals(familyKey(null, "Staphylococcaceae"), familyKey(null, "staphylococcaceae"))
        assertEquals(familyKey("ف", null), familyKey("ف", null))
        assertEquals("general_family", familyKey(null, null))
        assertTrue(familyKey("F", null).isNotBlank())
    }

    @Test
    fun `normalization unifies Arabic and Persian letter forms and separators`() {
        assertEquals("باسیل", normalizeTaxonomyKey("باسيل"))
        assertEquals("باسیل", normalizeTaxonomyKey("باسیل"))
        assertEquals("ab", normalizeTaxonomyKey("a\u200cb"))
        assertEquals("ab", normalizeTaxonomyKey("a-b"))
        assertEquals("", normalizeTaxonomyKey("   "))
        assertEquals("", normalizeTaxonomyKey(null))
    }

    @Test
    fun `clean class name strips the parenthesised genus`() {
        assertEquals("باسیل", cleanClassName("باسیل (سرده Bacillus)"))
        assertEquals("باسیل", cleanClassName("باسیل – سرده Bacillus"))
        assertEquals("", cleanClassName(null))
    }

    @Test
    fun `search matches taxonomy names and pathogen names`() {
        val list = listOf(
            disease(id = 1, genus = "باسیلوس", genusEn = "Bacillus", family = "باسیلاسه", klass = "باسیل"),
            disease(id = 2, genus = "ویروس", genusEn = "Virus", family = "ویروسی", klass = "ویروس")
        )

        assertEquals(1, buildTaxonomyTree(list, "باسیلوس").sumOf { it.diseases.size })
        assertEquals(1, buildTaxonomyTree(list, "bacillus").sumOf { it.diseases.size })
        assertEquals(0, buildTaxonomyTree(list, "ناموجود").sumOf { it.diseases.size })
        assertEquals(2, buildTaxonomyTree(list, "").sumOf { it.diseases.size })
    }

    @Test
    fun `matches query ignores Persian and Arabic letter forms`() {
        val d = disease(id = 1, genus = "باسیلوس")
        assertTrue(matchesQuery(d, "باسيلوس"))
        assertTrue(matchesQuery(d, "باسیلوس"))
        assertFalse(matchesQuery(d, "xxx"))
    }

    @Test
    fun `search tolerates Persian transliteration variants`() {
        val d = disease(id = 51, nameFa = "سیفیلیس", genusEn = "Treponema")
        assertTrue(matchesQuery(d, "سفلی"))
        assertTrue(matchesQuery(d, "سیفلیس"))
        assertTrue(matchesQuery(d, "سفلیس"))
        assertTrue(matchesQuery(d, "سیفلس"))
        assertTrue(matchesQuery(d, "سیفیلیس"))
    }

    @Test
    fun `search tolerates English spelling mistakes`() {
        val d = disease(id = 1, genusEn = "Staphylococcus", agentEn = "Staphylococcus aureus")
        assertTrue(matchesQuery(d, "Stafilococcus"))
        assertTrue(matchesQuery(d, "staph"))
        assertTrue(matchesQuery(d, "aureos"))
        assertFalse(matchesQuery(d, "xyz"))
    }

    @Test
    fun `search ignores a missing ZWNJ`() {
        val d = disease(id = 2, nameFa = "می‌شود")
        assertTrue(matchesQuery(d, "میشود"))
        assertTrue(matchesQuery(d, "می شود"))
    }

    @Test
    fun `search does not return unrelated records`() {
        val d = disease(id = 3, nameFa = "سیفیلیس")
        assertFalse(matchesQuery(d, "گلوما"))
        assertFalse(matchesQuery(d, "ززززززززززز"))
    }

    @Test
    fun `every query word must match`() {
        val d = disease(id = 4, nameFa = "آنفلوانزای C", category = "ویروس‌ها")
        assertTrue(matchesQuery(d, "ویروس انفلوانزا"))
        assertFalse(matchesQuery(d, "ویروس گلوما"))
    }

    @Test
    fun `flatten respects expanded state at every level`() {
        val list = listOf(
            disease(id = 1, genus = "GA", genusEn = "GA", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA"),
            disease(id = 2, genus = "GB", genusEn = "GB", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA")
        )
        val tree = buildTaxonomyTree(list, "")
        val cat = tree[0]
        val cls = cat.classes[0]
        val fam = cls.families[0]

        // فقط دسته باز است: رده‌ها دیده می‌شوند تا بتوان آن‌ها را باز کرد
        val onlyCat = flatten(tree, mapOf(cat.key to true))
        assertEquals(listOf("cat", "cls"), onlyCat.map { it.stableKey.substringBefore(':') })
        assertEquals(2, onlyCat.size)

        // دسته و رده باز: خانواده‌ها دیده می‌شوند
        val catAndClass = flatten(tree, mapOf(cat.key to true, cls.key to true))
        assertEquals(listOf("cat", "cls", "fam"), catAndClass.map { it.stableKey.substringBefore(':') })

        // همه باز
        val all = flatten(tree, mapOf(cat.key to true, cls.key to true, fam.key to true))
        assertEquals(listOf("cat", "cls", "fam", "dis", "dis"), all.map { it.stableKey.substringBefore(':') })
        assertEquals(5, all.size)
    }

    @Test
    fun `flatten omits empty groups when searching`() {
        val list = listOf(
            disease(id = 1, genus = "GA", genusEn = "GA", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA")
        )
        val tree = buildTaxonomyTree(list, "ناموجود")

        assertTrue(tree.isEmpty())
        assertTrue(flatten(tree, emptyMap()).isEmpty())
    }

    @Test
    fun `flatten keeps a stable key for every row`() {
        val list = listOf(
            disease(id = 1, genus = "GA", genusEn = "GA", family = "FA", familyEn = "FA", klass = "CA", classEn = "CA")
        )
        val tree = buildTaxonomyTree(list, "")
        val all = flatten(tree, mapOf(tree[0].key to true, tree[0].classes[0].key to true, tree[0].classes[0].families[0].key to true))

        assertEquals(all.map { it.stableKey }, all.map { it.stableKey }.distinct())
        assertTrue(all.any { it is TaxEntry.Pathogen && it.stableKey == "dis:1" })
    }

    // ── پیوندهای عمیق (منطق خالص) ────────────────────────────────────────────

    @Test
    fun `disease deep link parses the numeric id from the slug form`() {
        val link = DeepLinks.parse("pathodex.ir", listOf("disease", "12-عفونت-x"))
        assertEquals("disease", link?.route)
        assertEquals(12, link?.id)
        assertEquals("12-عفونت-x", link?.slug)
    }

    @Test
    fun `disease deep link parses the two segment form`() {
        assertEquals(12, DeepLinks.parse("pathodex.ir", listOf("disease", "12", "x"))?.id)
    }

    @Test
    fun `category deep link keeps the slug`() {
        val link = DeepLinks.parse("pathodex.ir", listOf("category", "باکتریایی"))
        assertEquals("category", link?.route)
        assertEquals("باکتریایی", link?.slug)
    }

    @Test
    fun `content routes are recognised`() {
        for (route in listOf("taxonomy", "study", "quiz", "compare", "about")) {
            assertEquals(route, DeepLinks.parse("pathodex.ir", listOf(route))?.route)
        }
    }

    @Test
    fun `foreign hosts and unknown paths are ignored`() {
        assertNull(DeepLinks.parse("evil.example", listOf("disease", "1-x")))
        assertNull(DeepLinks.parse("pathodex.ir", listOf("unknown")))
        assertNull(DeepLinks.parse("pathodex.ir", listOf("disease", "not-a-number-x")))
        assertNull(DeepLinks.parse("pathodex.ir", emptyList()))
        assertNull(DeepLinks.parse(null, listOf("disease", "1-x")))
        assertNull(DeepLinks.parse("pathodex.ir", listOf("disease")))
    }

    @Test
    fun `deep link parse is case insensitive on the host`() {
        assertEquals(3, DeepLinks.parse("PATHODEX.IR", listOf("disease", "3-x"))?.id)
        assertEquals(3, DeepLinks.parse("PathoDex.ir", listOf("disease", "3-x"))?.id)
    }

    @Test
    fun `deep link ids stop at the first dash`() {
        assertEquals(1000090, DeepLinks.parse("pathodex.ir", listOf("disease", "1000090-x"))?.id)
        assertNull(DeepLinks.parse("pathodex.ir", listOf("disease", "x-1000090"))?.id)
    }

    // ── برچسب‌های محدوده میزبان ────────────────────────────────────────────────

    @Test
    fun `disease model exposes the website host scope labels`() {
        assertEquals("پاتوژن انسانی", disease(id = 1).copy(host_scope = "human").hostScopeLabel)
        assertEquals("پاتوژن غیرانسانی", disease(id = 1).copy(host_scope = "non_human").hostScopeLabel)
        assertEquals("انسانی و غیرانسانی", disease(id = 1).copy(host_scope = "both").hostScopeLabel)
        assertEquals("—", disease(id = 1).hostScopeLabel)
    }
}
