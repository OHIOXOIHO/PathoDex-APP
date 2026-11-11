package com.example.data.auth

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * ورود با گوگل از طریق Credential Manager (راهکار امروزی اندروید).
 *
 * این لایه فقط یک id_token می‌گیرد. اعتبارسنجی واقعی — امضا، مخاطب، ایمیل
 * تأییدشده — سمت سرور pathodex.ir انجام می‌شود چون راز گوگل فقط آنجاست.
 *
 * مخاطب توکن (aud) همان کارخواه وب است که در BuildConfig.GOOGLE_WEB_CLIENT_ID
 * تعریف شده؛ سرور هم با همان مقدار توکن را می‌پذیرد.
 *
 * نکته‌ی مهم راه‌اندازی: گوگل انتخابگر حساب را فقط وقتی نشان می‌دهد که
 * «کارخواه اندرویدی» با همین نام بسته و همین اثرانگشت SHA-1 در همان پروژه‌ی
 * Google Cloud که کارخواه وب در آن است ثبت شده باشد. بدون آن ثبت،
 * Credential Manager هیچ حسابی برنمی‌گرداند — که پیام واقعی آن «راه‌اندازی
 * ناقص است»، نه «روی دستگاه حسابی وجود ندارد».
 */
object GoogleAuthHelper {

    private const val TAG = "GoogleAuthHelper"

    /** نوع خرابی برای پیام‌های دقیق و قابل‌اقدام. */
    enum class FailureKind {
        /** کاربر انتخابگر را بسته یا پشتیبانی لغو کرده است. */
        CANCELLED,

        /** روی دستگاه هیچ حساب گوگلی فعالی وجود ندارد. */
        NO_ACCOUNT,

        /**
         * اپلیکیشن در Google Cloud ثبت نشده است (نام بسته یا اثرانگشت ناسازگار).
         * رایج‌ترین علت وقتی خطا می‌گوید «حسابی موجود نیست».
         */
        NOT_REGISTERED,

        /** Google Play Services نصب یا به‌روز نیست. */
        PLAY_SERVICES_UNAVAILABLE,

        /** ورود با گوگل روی این دستگاه پشتیبانی نمی‌شود. */
        UNSUPPORTED,

        /** فرایند ورود ناتمام ماند. */
        INTERRUPTED,

        /** خطای دیگر. */
        OTHER
    }

    /** خروجی ناموفق با پیام فارسی و اطلاعات تشخیص‌دهی برای نمایش به کاربر. */
    class GoogleSignInException(
        val kind: FailureKind,
        message: String,
        val diagnostics: String
    ) : Exception(message)

    /**
     * انتخاب حساب گوگل و دریافت id_token.
     *
     * @param activity لازم است چون انتخابگر حساب در قالب یک Activity نمایش داده می‌شود.
     */
    suspend fun signIn(activity: Activity): Result<String> {
        val playServicesOk = isPlayServicesAvailable(activity)
        if (!playServicesOk) {
            return Result.failure(
                GoogleSignInException(
                    FailureKind.PLAY_SERVICES_UNAVAILABLE,
                    "برای ورود با گوگل، Google Play Services باید نصب و به‌روز باشد.",
                    diagnostics(activity)
                )
            )
        }

        return try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setNonce(nonce())
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val manager = CredentialManager.create(activity)
            val response = manager.getCredential(activity, request)

            val idToken = extractIdToken(response)
            if (idToken.isNullOrBlank()) {
                Result.failure(
                    GoogleSignInException(
                        FailureKind.OTHER,
                        "گوگل نشانه‌ی هویتی برنگرداند. دوباره تلاش کنید.",
                        diagnostics(activity)
                    )
                )
            } else {
                Result.success(idToken)
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(
                GoogleSignInException(FailureKind.CANCELLED, "ورود با گوگل لغو شد.", diagnostics(activity))
            )
        } catch (e: NoCredentialException) {
            // این خطا معمولاً یعنی اپلیکیشن در Google Cloud ثبت نشده است،
            // نه اینکه روی دستگاه حسابی وجود نداشته باشد.
            Result.failure(
                GoogleSignInException(
                    FailureKind.NOT_REGISTERED,
                    "گوگل حسابی برای این اپلیکیشن برنگرداند. اپلیکیشن در Google Cloud ثبت نشده است.",
                    diagnostics(activity)
                )
            )
        } catch (e: GetCredentialProviderConfigurationException) {
            Result.failure(
                GoogleSignInException(
                    FailureKind.NOT_REGISTERED,
                    "کتابخانه‌ی ورود گوگل روی این دستگاه پیکربندی نشده است.",
                    diagnostics(activity)
                )
            )
        } catch (e: GetCredentialUnsupportedException) {
            Result.failure(
                GoogleSignInException(
                    FailureKind.UNSUPPORTED,
                    "ورود با گوگل روی این دستگاه پشتیبانی نمی‌شود.",
                    diagnostics(activity)
                )
            )
        } catch (e: GetCredentialInterruptedException) {
            Result.failure(
                GoogleSignInException(FailureKind.INTERRUPTED, "فرایند ورود ناتمام ماند.", diagnostics(activity))
            )
        } catch (e: GetCredentialException) {
            val kind = if (e.message?.contains("account", ignoreCase = true) == true) {
                FailureKind.NO_ACCOUNT
            } else {
                FailureKind.OTHER
            }
            Result.failure(
                GoogleSignInException(kind, "ورود با گوگل ناموفق بود.", diagnostics(activity))
            )
        } catch (e: Exception) {
            Result.failure(
                GoogleSignInException(
                    FailureKind.OTHER,
                    "ورود با گوگل ممکن نشد: ${e.localizedMessage ?: ""}".trim(),
                    diagnostics(activity)
                )
            )
        }
    }

    /**
     * استخراج id_token از پاسخ Credential Manager.
     *
     * معمولاً پاسخ GoogleIdTokenCredential است؛ اما بعضی از مسیرها یک
     * CustomCredential با همان نوع برمی‌گردانند که نشانه در باندل آن است.
     */
    private fun extractIdToken(response: androidx.credentials.GetCredentialResponse): String? {
        val credential = response.credential
        if (credential is GoogleIdTokenCredential) return credential.idToken
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            // باندل ردیفی از کارخواه گوگل؛ نشانه در کلید استاندارد آن است.
            return credential.data.getString(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                ?.takeIf { it.isNotBlank() }
                ?: credential.data.getString("id_token")
                ?: credential.data.getString("google_id_token")
        }
        return null
    }

    /** خروج از حساب گوگل انتخاب‌شده تا انتخابگر در دفعه‌ی بعد دوباره باز شود. */
    suspend fun signOut(activity: Activity) {
        try {
            CredentialManager.create(activity)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // خروج از گوگل اختیاری است؛ خطایش نباید ورود را متوقف کند
        }
    }

    private fun isPlayServicesAvailable(activity: Activity): Boolean = try {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS
    } catch (e: Exception) {
        Log.w(TAG, "Play Services check failed", e)
        // اگر خود بررسی ممکن نشد، اجازه بده مسیر اصلی تلاش کند
        true
    }

    /**
     * اطلاعات تشخیص‌دهی برای نمایش در پیام خطا.
     *
     * نام بسته و اثرانگشت SHA-1 دقیق همان چیزهایی هستند که باید در Google Cloud
     * ثبت شوند؛ نمایش آن‌ها به کاربر اجازه می‌دهد مشکل راه‌اندازی را دقیق برطرف کند.
     */
    fun diagnostics(activity: Activity): String {
        val sha1 = signingSha1(activity)
        return buildString {
            append("پاکت: ").append(activity.packageName).append('\n')
            append("SHA-1: ").append(sha1 ?: "نامشخص").append('\n')
            append("کارخواه وب: ")
            append(BuildConfig.GOOGLE_WEB_CLIENT_ID.substringBefore(".apps.googleusercontent.com"))
            append('\n')
            append("راه‌حل: در Google Cloud Console یک OAuth Client از نوع Android ")
            append("با همین پاکت و همین SHA-1 بسازید و در همان پروژه‌ای که کارخواه وب ")
            append("در آن است ثبت کنید.")
        }
    }

    /** اثرانگشت SHA-1 امضای فعلی اپلیکیشن (دیباگ یا رلیز). */
    private fun signingSha1(activity: Activity): String? = try {
        val signers: Array<android.content.pm.Signature>? = if (Build.VERSION.SDK_INT >= 28) {
            activity.packageManager
                .getPackageInfo(activity.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo
                ?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            activity.packageManager
                .getPackageInfo(activity.packageName, PackageManager.GET_SIGNATURES)
                .signatures
        }
        val digest = MessageDigest.getInstance("SHA-1")
        signers?.firstOrNull()?.let { signer ->
            digest.update(signer.toByteArray())
            digest.digest().joinToString(":") { "%02X".format(it) }
        }
    } catch (e: Exception) {
        Log.w(TAG, "Could not read signing certificate", e)
        null
    }

    private fun nonce(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it) }
}
