package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "email") val email: String? = null
)

/**
 * ورود با گوگل: id_token که کتابخانه‌ی گوگل برای کارخواه وب ما صادر می‌کند
 * به سرور فرستاده می‌شود تا امضا، مخاطب و ایمیل را اعتبارسنجی کند.
 */
@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "id_token") val idToken: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "email_verified") val email_verified: Boolean? = false,
    @Json(name = "dev_code") val dev_code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "role") val role: String, // 'owner', 'admin', 'user'
    @Json(name = "email") val email: String? = null,
    @Json(name = "email_verified") val email_verified: Boolean? = false,
    @Json(name = "created_at") val created_at: String? = null
)

/** درخواست تغییر نام‌کاربری (POST /api/me/username). */
@JsonClass(generateAdapter = true)
data class UsernameChangeRequest(
    @Json(name = "username") val username: String
)

/** درخواست ارسال کد تأیید به ایمیل حساب (POST /api/me/email/send-code). */
@JsonClass(generateAdapter = true)
data class EmailCodeRequest(
    @Json(name = "email") val email: String
)

/** درخواست تأیید ایمیل با کد یک‌بارمصرف (POST /api/me/email/verify). */
@JsonClass(generateAdapter = true)
data class VerifyEmailRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String
)

/** درخواست بازنشاندن رمز با کد بازیابی، بدون رمز فعلی (POST /api/me/password/reset). */
@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String,
    @Json(name = "newPassword") val newPassword: String
)

/** درخواست کد بازیابی رمز برای حساب بدون ورود (POST /api/auth/forgot-password). */
@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @Json(name = "email") val email: String
)

/**
 * پاسخ عمومی مسیرهایی که کد و پیام برمی‌گردانند (ارسال کد، تأیید، بازنشانی).
 * فیلد retry_after برای مکث ۶۰ ثانیه‌ای ارسال مجدد است.
 */
@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "dev_code") val dev_code: String? = null,
    @Json(name = "retry_after") val retry_after: Int? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequest(
    @Json(name = "oldPassword") val oldPassword: String,
    @Json(name = "newPassword") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class InteractRequest(
    @Json(name = "action") val action: String, // 'favorite', 'bookmark', 'learned', 'note'
    @Json(name = "value") val value: Any
)

@JsonClass(generateAdapter = true)
data class StatsResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "data") val data: StatsData? = null
)

@JsonClass(generateAdapter = true)
data class StatsData(
    @Json(name = "total_diseases") val total_diseases: Int = 0,
    @Json(name = "total_images") val total_images: Int = 0,
    @Json(name = "categories") val categories: List<CategoryStat> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CategoryStat(
    @Json(name = "category") val category: String,
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class CategoriesResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<CategoryStat> = emptyList()
)
