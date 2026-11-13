package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pathodex_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("jwt_token", null)
        set(value) = prefs.edit().putString("jwt_token", value).apply()

    var username: String?
        get() = prefs.getString("username", null)
        set(value) = prefs.edit().putString("username", value).apply()

    var role: String?
        get() = prefs.getString("user_role", null)
        set(value) = prefs.edit().putString("user_role", value).apply()

    /** ایمیل حسابی که با آن وارد شده‌ایم (ورود با گوگل یا ثبت‌نام ایمیلی). */
    var email: String?
        get() = prefs.getString("user_email", null)
        set(value) = prefs.edit().putString("user_email", value).apply()

    var nsfwAccepted: Boolean
        get() = prefs.getBoolean("nsfw_accepted", false)
        set(value) = prefs.edit().putBoolean("nsfw_accepted", value).apply()

    var blurImages: Boolean
        get() = prefs.getBoolean("blur_images", false)
        set(value) = prefs.edit().putBoolean("blur_images", value).apply()

    var isCompactListMode: Boolean
        get() = prefs.getBoolean("is_compact_list", true) // Default to compact list for excellent information density!
        set(value) = prefs.edit().putBoolean("is_compact_list", value).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system" // "system", "dark", "light"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    /** شناسه‌ی رنگ accent انتخاب‌شده (پیش‌فرض: «میدنایت بنفش»). */
    var accentId: String
        get() = prefs.getString("accent_id", "midnight") ?: "midnight"
        set(value) = prefs.edit().putString("accent_id", value).apply()

    /**
     * آیا پیوندهای pathodex.ir در اپلیکیشن باز شوند؟
     *
     * پیش‌فرض true است تا اپلیکیشن اصلاً شناخته نشود؛ اما چون دامنه تأیید
     * نشده است (autoVerify نداریم)، اندروید هر بار می‌پرسد که پیوند در مرورگر
     * باشد یا در اپلیکیشن. کاربری که سامانه‌ی وب را ترجیح می‌دهد مرورگر را
     * انتخاب می‌کند و همان‌جا می‌ماند. اگر این کلید خاموش شود، اپلیکیشن اصلاً
     * پیوند نمی‌گیرد و همه‌ی پیوندها در مرورگر باز می‌شوند.
     */
    var deepLinksEnabled: Boolean
        get() = prefs.getBoolean("deep_links_enabled", true)
        set(value) = prefs.edit().putBoolean("deep_links_enabled", value).apply()

    /**
     * زمان آخرین همگام‌سازی موفق فهرست پاتوژن‌ها (epoch میلی‌ثانیه).
     * تا وقتی که این مقدار تازه است، فهرست از حافظه‌ی محلی خوانده می‌شود و
     * دوباره از سرور دانلود نمی‌شود.
     */
    var lastDiseasesSyncAt: Long
        get() = prefs.getLong("last_diseases_sync_at", 0L)
        set(value) = prefs.edit().putLong("last_diseases_sync_at", value).apply()

    var monographFontSize: Float
        get() = prefs.getFloat("monograph_font_size", 14.5f)
        set(value) = prefs.edit().putFloat("monograph_font_size", value).apply()

    fun getLocalNote(diseaseId: Int): String {
        return prefs.getString("note_$diseaseId", "") ?: ""
    }

    fun saveLocalNote(diseaseId: Int, note: String) {
        prefs.edit().putString("note_$diseaseId", note).apply()
    }

    fun clearAuth() {
        prefs.edit()
            .remove("jwt_token")
            .remove("username")
            .remove("user_role")
            .remove("user_email")
            .apply()
    }
}
