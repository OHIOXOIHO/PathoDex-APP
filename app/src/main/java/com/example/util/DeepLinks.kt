package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.example.data.model.SiteUrls

/**
 * کلید انتخاب کاربر برای باز کردن پیوندهای pathodex.ir در اپلیکیشن.
 *
 * پیوندهای عمیق در یک [android.app.Activity] alias تعریف شده‌اند
 * (`activity-alias` با نام `.DeepLink`) تا بتوان آن‌ها را از نظر سیستم‌عامل
 * فعال یا غیرفعال کرد. وقتی غیرفعال است، همان پیوندها در مرورگر وب باز می‌شوند
 * و اپلیکیشن دخالتی ندارد — یعنی کاربر انتخاب می‌کند، نه نصب اپلیکیشن.
 *
 * نام کامپوننت باید با نام کامل alias در Manifest یکی باشد (`com.example.DeepLink`)،
 * چون `android:name=".DeepLink"` نسبت به namespace برنامه حل می‌شود.
 */
object DeepLinks {

    /** نام کامپوننت activity-alias که پیوندهای عمیق روی آن تعریف شده است.
     *
     * نام alias در Manifest نسبت به namespace برنامه (`com.example`) حل می‌شود،
     * پس کامپوننت واقعی `com.example.DeepLink` است — نه applicationId.
     */
    const val ALIAS_NAME = "com.example.DeepLink"

    /** یک پیوند عمیق وب که در اپلیکیشن معادل دارد. */
    data class Link(val route: String, val id: Int?, val slug: String?)

    /**
     * تبدیل نشانی pathodex.ir به مسیر داخلی.
     *
     * فقط مسیرهایی که اپلیکیشن واقعاً می‌شناسد برگردانده می‌شوند؛ بقیه null است تا
     * کاربر بدون خطا در همان صفحه‌ی قبلی بماند. شناسه‌ی پرونده از خود نشانی
     * خوانده می‌شود، نه از نام نمایشی.
     */
    fun parse(uri: Uri?): Link? {
        if (uri == null) return null
        return parse(uri.host, uri.pathSegments)
    }

    /**
     * همان منطق مسیریابی بدون وابستگی به [Uri]، تا در آزمون واحد هم قابل‌استفاده باشد.
     *
     * @param host میزبان نشانی (مثل pathodex.ir)
     * @param segments بخش‌های مسیر (بدون رشته‌ی خالی)
     */
    fun parse(host: String?, segments: List<String>): Link? {
        if (!host.equals(SiteUrls.HOST.removePrefix("https://"), ignoreCase = true)) return null
        if (segments.isEmpty()) return null
        return when (segments.first()) {
            "disease" -> {
                val raw = segments.getOrNull(1) ?: return null
                // شکل نشانی: /disease/1-عفونت-... یا /disease/1/...
                val id = raw.substringBefore('-').substringBefore('/').toIntOrNull() ?: return null
                Link("disease", id, raw)
            }
            "category" -> Link("category", null, segments.getOrNull(1))
            "taxonomy" -> Link("taxonomy", null, null)
            "study" -> Link("study", null, null)
            "quiz" -> Link("quiz", null, null)
            "compare" -> Link("compare", null, null)
            "about" -> Link("about", null, null)
            else -> null
        }
    }

    /** اعمال انتخاب کاربر روی activity-alias. */
    fun apply(context: Context, enabled: Boolean) {
        return try {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(context.packageName, ALIAS_NAME),
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Exception) {
            // اگر سیستم اجازه نداد (مثلاً روی برخی سیستم‌های سفارشی)، کاربر همچنان
            // از مسیر تنظیمات اندروید می‌تواند پیش‌فرض را عوض کند.
        }
    }

    /** آیا در حال حاضر پیوندهای عمیق فعال‌اند؟ (پیش‌فرض Manifest و تنظیمات: خاموش) */
    fun isEnabled(context: Context): Boolean {
        val state = context.packageManager.getComponentEnabledSetting(
            ComponentName(context.packageName, ALIAS_NAME)
        )
        return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
}
