package com.example.data.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * جستجوی خطاپذیر (typo-tolerant) برای فارسی و انگلیسی — هم‌گام با سامانه‌ی وب.
 *
 * کاربر حروف عربی را به‌جای فارسی می‌نویسد (ي به‌جای ی، ك به‌جای ک)، نیم‌فاصله را
 * فراموش می‌کند، یا در انگلسی غلط املایی دارد. این ماژول متن را نرمال می‌کند و
 * سپس با فاصله‌ی ویراش، شباهت جارو-وینکلر و زیروالی‌بودن تطبیق می‌دهد.
 *
 * منطق دقیقاً همان چیزی است که در PathoSearch سامانه‌ی وب است تا رفتار دو سازه
 * با هم یکی باشد.
 */
object SearchMatcher {

    private val TOKEN_SPLIT = Regex("[\\s,;()\\[\\]·•|/\\-+]+")

    data class SearchQuery(
        val empty: Boolean,
        val q: String,
        val qsquash: String,
        val tokens: List<String>
    )

    data class IndexedPool(
        val norm: String,
        val squash: String,
        val tokens: List<String>
    )

    data class MatchResult(val match: Boolean, val score: Int)

    private val poolCache = LinkedHashMap<String, IndexedPool>()

    /** یکدست‌سازی حروف عربی/فارسی، حرکات، نیم‌فاصله و ارقام. */
    fun normalize(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return value
            .replace('\u064A', '\u06CC')   // ي -> ی
            .replace('\u0649', '\u06CC')   // ى -> ی
            .replace('\u06CD', '\u06CC')   // ۍ -> ی
            .replace('\u06D2', '\u06CC')   // ے -> ی
            .replace('\u0643', '\u06A9')   // ك -> ک
            .replace('\u0622', '\u0627')   // آ -> ا
            .replace('\u0623', '\u0627')   // أ -> ا
            .replace('\u0625', '\u0627')   // إ -> ا
            .replace('\u0671', '\u0627')   // ٱ -> ا
            .replace('\u0629', '\u0647')   // ة -> ه
            .replace('\u06C0', '\u0647')   // ۀ -> ه
            .replace('\u06C1', '\u0647')   // ہ -> ه
            .replace("\u0640", "")         // تطویل
            .replace(Regex("[\\u064B-\\u0655\\u0670]"), "")   // حرکات عربی
            .replace('\u200C', ' ')        // نیم‌فاصله -> فاصله
            .replace(Regex("[\\u200D\\u200E\\u200F\\uFEFF]"), "")
            .replace(Regex("[\\u0660-\\u0669]")) { m ->
                (0x06F0 + (m.value[0].code - 0x0660)).toChar().toString()
            }
            .lowercase(Locale.ROOT)
            .trim()
    }

    /** فاصله‌ی ویراش با خروج زودهنگام وقتی نتیجه از max بیشتر شود. */
    fun editDistance(a: String, b: String, max: Int): Int {
        if (abs(a.length - b.length) > max) return max + 1
        val prev = IntArray(b.length + 1)
        val cur = IntArray(b.length + 1)
        for (j in 0..b.length) prev[j] = j
        for (i in 1..a.length) {
            cur[0] = i
            var rowMin = cur[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                val v = min(prev[j] + 1, min(cur[j - 1] + 1, prev[j - 1] + cost))
                cur[j] = v
                if (v < rowMin) rowMin = v
            }
            if (rowMin > max) return max + 1
            System.arraycopy(cur, 0, prev, 0, b.length + 1)
        }
        return prev[b.length]
    }

    private fun jaro(a: String, b: String): Double {
        if (a == b) return 1.0
        val la = a.length
        val lb = b.length
        if (la == 0 || lb == 0) return 0.0
        val window = max(la, lb) / 2 - 1
        if (window < 0) return 0.0
        val aMatch = BooleanArray(la)
        val bMatch = BooleanArray(lb)
        var matches = 0
        for (i in 0 until la) {
            val lo = max(0, i - window)
            val hi = min(i + window + 1, lb)
            for (j in lo until hi) {
                if (!bMatch[j] && a[i] == b[j]) {
                    aMatch[i] = true
                    bMatch[j] = true
                    matches++
                    break
                }
            }
        }
        if (matches == 0) return 0.0
        var t = 0
        var k = 0
        for (i in 0 until la) {
            if (!aMatch[i]) continue
            while (!bMatch[k]) k++
            if (a[i] != b[k]) t++
            k++
        }
        t /= 2
        return (matches.toDouble() / la + matches.toDouble() / lb + (matches - t).toDouble() / matches) / 3.0
    }

    /** شباهت جارو-وینکلر: بهتر از فاصله‌ی ویراش برای جابه‌جایی حروف و پیشوند مشترک. */
    fun jaroWinkler(a: String, b: String): Double {
        val j = jaro(a, b)
        var prefix = 0
        val maxPrefix = min(4, min(a.length, b.length))
        while (prefix < maxPrefix && a[prefix] == b[prefix]) prefix++
        return j + prefix * 0.1 * (1 - j)
    }

    private fun isSubsequence(short: String, long: String): Boolean {
        var i = 0
        for (j in 0 until long.length) {
            if (i < short.length && short[i] == long[j]) i++
        }
        return i == short.length
    }

    /**
     * واریانت‌های ترجمه‌ی صوتی: «سفلیس» زیروالی صی «سیفیلیس» است.
     * فقط وقتی صدق می‌کند که طولشان خیلی متفاوت نباشد تا مثبت کاذب نگیرد.
     * آستانه ۰٫۵۵ است تا نشانی‌های ناقص هم هنگام تایپ نتیجه بدهند («سفلی»).
     */
    fun subseqMatch(a: String, b: String): Boolean {
        val short = if (a.length <= b.length) a else b
        val long = if (a.length <= b.length) b else a
        if (short.length < 4 || short.length.toDouble() / long.length < 0.55) return false
        return isSubsequence(short, long)
    }

    fun prepareQuery(query: String): SearchQuery {
        val q = normalize(query)
        if (q.isEmpty()) return SearchQuery(true, "", "", emptyList())
        return SearchQuery(
            empty = false,
            q = q,
            qsquash = q.replace(" ", ""),
            tokens = q.split(TOKEN_SPLIT).filter { it.isNotEmpty() }
        )
    }

    @Synchronized
    fun indexPool(pool: String): IndexedPool {
        poolCache[pool]?.let { return it }
        val norm = normalize(pool)
        val entry = IndexedPool(
            norm = norm,
            squash = norm.replace(" ", ""),
            tokens = norm.split(TOKEN_SPLIT).filter { it.isNotEmpty() }
        )
        if (poolCache.size > 800) poolCache.clear()
        poolCache[pool] = entry
        return entry
    }

    fun matchPrepared(prepared: SearchQuery, pool: IndexedPool): MatchResult {
        if (prepared.empty) return MatchResult(true, 0)

        // نیم‌فاصله و فاصله معمولی یکی هم هستند: «میشود» با «می‌شود» و «می شود» یکی شود
        if (pool.norm.contains(prepared.q)
            || (prepared.qsquash.length >= 2 && pool.squash.contains(prepared.qsquash))
        ) {
            return MatchResult(true, 100)
        }
        if (prepared.tokens.isEmpty()) return MatchResult(true, 0)

        var matched = 0
        var penalty = 0
        for (qt in prepared.tokens) {
            var best = Int.MAX_VALUE
            var bestJw = 0.0
            var bestPrefix = 0
            var bestSubseq = false
            for (pt in pool.tokens) {
                if (pt.contains(qt)) { best = 0; bestJw = 1.0; break }
                val d = editDistance(qt, pt, 3)
                if (d < best) best = d
                var p = 0
                val maxP = min(qt.length, pt.length)
                while (p < maxP && qt[p] == pt[p]) p++
                if (p > bestPrefix) bestPrefix = p
                if (p >= 2 && pt.length >= 4) {
                    val jw = jaroWinkler(qt, pt)
                    if (jw > bestJw) bestJw = jw
                }
                if (!bestSubseq && p > 0 && subseqMatch(qt, pt)) bestSubseq = true
            }
            val allowed = when {
                qt.length <= 4 -> 1
                qt.length <= 7 -> 1
                qt.length <= 11 -> 2
                else -> 3
            }
            var cost = Int.MAX_VALUE
            if (best <= allowed) cost = best
            else if (bestJw >= 0.90) cost = 1
            else if (isLatin(qt) && bestPrefix >= 5 && qt.length >= 8) cost = 1
            else if (bestSubseq) cost = 2
            if (cost != Int.MAX_VALUE) {
                matched++
                penalty += cost
            }
        }

        if (matched == 0) return MatchResult(false, 0)
        // همه‌ی واژه‌های پرس‌وجو باید یافت شوند؛ فقط در پرس‌وجوی ۳ واژه‌ای یا بیشتر
        // یک واژه‌ی غلط‌املایی‌شده مجاز است تا کل نتیجه از دست نرود.
        val allowedMiss = if (prepared.tokens.size >= 3) 1 else 0
        if (prepared.tokens.size - matched > allowedMiss) return MatchResult(false, 0)
        val score = max(1, (matched.toDouble() / prepared.tokens.size * 80).roundToInt() - penalty)
        return MatchResult(true, score)
    }

    fun match(query: String, pool: IndexedPool): MatchResult =
        matchPrepared(prepareQuery(query), pool)

    private fun isLatin(s: String): Boolean = s.all { it.code in 0x20..0x7E }
}
