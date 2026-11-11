package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.api.ApiClient
import com.example.data.local.DiseaseDao
import com.example.data.local.DiseaseEntity
import com.example.data.local.PathodexDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.AuthResponse
import com.example.data.model.Disease
import com.example.data.model.EmailCodeRequest
import com.example.data.model.ForgotPasswordRequest
import com.example.data.model.GoogleLoginRequest
import com.example.data.model.LoginRequest
import com.example.data.model.MessageResponse
import com.example.data.model.RegisterRequest
import com.example.data.model.ResetPasswordRequest
import com.example.data.model.TaxonomyRefs
import com.example.data.model.UpdatePasswordRequest
import com.example.data.model.User
import com.example.data.model.UsernameChangeRequest
import com.example.data.model.VerifyEmailRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PathodexRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = PathodexDatabase.getDatabase(appContext)
    private val dao: DiseaseDao = db.diseaseDao()
    val prefs = UserPreferences(appContext)
    private val api = ApiClient.service

    companion object {
        /**
         * مدت اعتبار حافظه‌ی محلیِ فهرست پاتوژن‌ها پیش از همگام‌سازی مجدد.
         * تا وقتی داده تازه است، فقط از دیتابیس محلی خوانده می‌شود و
         * کل فهرست دوباره دانلود نمی‌شود — این هم مصرف اینترنت را کم می‌کند
         * هم باز شدن برنامه را فوری و آفلاین‌پذیر می‌کند.
         */
        const val CACHE_TTL_MS = 12L * 60L * 60L * 1000L
    }

    /** آیا دستگاه الان به اینترنت متصل است؟ (برای نمایش حالت آفلاین) */
    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    val diseasesFlow: Flow<List<Disease>> = dao.getAllDiseases().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun loadDiseases(forceRefresh: Boolean = false): Result<List<Disease>> = withContext(Dispatchers.IO) {
        val cached = dao.getAllDiseasesSync()
        val cacheFresh = cached.isNotEmpty() &&
            prefs.lastDiseasesSyncAt > 0L &&
            (System.currentTimeMillis() - prefs.lastDiseasesSyncAt) < CACHE_TTL_MS

        // حافظه‌ی محلی تازه است: بدون دانلود مجدد، فقط همان را برگردان.
        if (!forceRefresh && cacheFresh) {
            return@withContext Result.success(cached.map { it.toDomain() })
        }

        // آفلاین و بدون حافظه تازه: بلافاصله همان داده‌ی محلی را برگردان
        if (!forceRefresh && cached.isNotEmpty() && !isNetworkAvailable()) {
            return@withContext Result.success(cached.map { it.toDomain() })
        }

        try {
            val token = prefs.token
            val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null
            val remoteDiseases = api.getDiseases(authHeader)

            if (remoteDiseases.isNotEmpty()) {
                val serverIds = remoteDiseases.map { it.id }
                // Purge any local disease that is NOT in the authoritative server list
                dao.deleteNotIn(serverIds)

                val localEntities = dao.getAllDiseasesSync().associateBy { it.id }
                val mergedEntities = mutableListOf<DiseaseEntity>()
                val actionsToPush = mutableListOf<Triple<Int, String, Any>>()

                for (remote in remoteDiseases) {
                    val local = localEntities[remote.id]
                    val mergedFav = (remote.is_favorite == true) || (local?.is_favorite == true)
                    val mergedBkm = (remote.is_bookmarked == true) || (local?.is_bookmarked == true)
                    val mergedLrn = (remote.is_learned == true) || (local?.is_learned == true)

                    // یادداشت: متن سامانه اولویت دارد، مگر اینکه کاربر متن تازه‌ای نوشته
                    // باشد که هنوز به سرور نرسیده است — آن‌وقت متن در انتظار ارسال را
                    // نشان می‌دهیم تا کاربر چیزی را از دست ندهد.
                    val pendingLocalNote = prefs.getLocalNote(remote.id)
                    val localDbNote = local?.personal_note ?: ""
                    val remoteNote = remote.personal_note?.takeIf { it.isNotBlank() } ?: ""
                    val mergedNote = when {
                        pendingLocalNote.isNotBlank() -> pendingLocalNote
                        remoteNote.isNotBlank() -> remoteNote
                        else -> localDbNote
                    }

                    // فهرست اصلی بخش‌های بالینی را نمی‌فرستد؛ بخش‌های ذخیره‌شده‌ی محلی حفظ شود
                    // تا پرونده‌ی باز هم آفلاین خوانده شود.
                    val mergedSections = if (remote.sections.isNotEmpty()) remote.sections
                    else (local?.sections ?: emptyList())

                    mergedEntities.add(
                        DiseaseEntity.fromDomain(
                            remote.copy(
                                sections = mergedSections,
                                is_favorite = mergedFav,
                                is_bookmarked = mergedBkm,
                                is_learned = mergedLrn,
                                personal_note = mergedNote
                            )
                        )
                    )

                    // If user is authenticated, sync local actions to the cloud account
                    if (!token.isNullOrBlank()) {
                        if (local?.is_favorite == true && remote.is_favorite != true) {
                            actionsToPush.add(Triple(remote.id, "favorite", true))
                        }
                        if (local?.is_bookmarked == true && remote.is_bookmarked != true) {
                            actionsToPush.add(Triple(remote.id, "bookmark", true))
                        }
                        if (local?.is_learned == true && remote.is_learned != true) {
                            actionsToPush.add(Triple(remote.id, "learned", true))
                        }
                        if (pendingLocalNote.isNotBlank() && pendingLocalNote != remoteNote) {
                            actionsToPush.add(Triple(remote.id, "note", pendingLocalNote))
                        }
                    }
                }

                dao.insertAll(mergedEntities)
                prefs.lastDiseasesSyncAt = System.currentTimeMillis()

                // Push actions to server asynchronously so login is instant and cloud sync stays current
                if (!token.isNullOrBlank() && actionsToPush.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        for ((id, action, value) in actionsToPush) {
                            try {
                                val json = JSONObject().apply {
                                    put("action", action)
                                    put("value", value)
                                }
                                val body = json.toString().toRequestBody("application/json".toMediaType())
                                api.interact(id, "Bearer $token", body)
                            } catch (_: Exception) {}
                        }
                    }
                }

                return@withContext Result.success(mergedEntities.map { it.toDomain() })
            } else {
                // If remote returns empty, return local data if available
                val local = dao.getAllDiseasesSync()
                return@withContext Result.success(local.map { it.toDomain() })
            }
        } catch (e: Exception) {
            // در صورت قطعی شبکه، داده‌ی محلی دست‌نخورده برگردانده می‌شود تا برنامه کاملاً آفلاین کار کند
            val offline = dao.getAllDiseasesSync()
            if (offline.isNotEmpty()) return@withContext Result.success(offline.map { it.toDomain() })
            return@withContext Result.failure(e)
        }
    }

    /**
     * دریافت پرونده‌ی کامل (با بخش‌های بالینی) از سرور و ذخیره‌ی بخش‌ها در دیتابیس محلی.
     * اگر کاربر وارد نشده باشد یا سرور در دسترس نباشد، داده‌ی محلی برگردانده می‌شود.
     */
    suspend fun loadDossier(id: Int): Result<Disease> = withContext(Dispatchers.IO) {
        val token = prefs.token
        val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null
        val local = dao.getDiseaseById(id)

        // آفلاین هستیم: بلافاصله از پرونده‌ی ذخیره‌شده استفاده کن تا باز شدن پرونده تأخیر نداشته باشد
        if (!isNetworkAvailable()) {
            return@withContext if (local != null) Result.success(local.toDomain())
            else Result.failure(Exception("پرونده در دستگاه موجود نیست"))
        }

        try {
            val res = api.getDisease(id, authHeader)
            if (res.isSuccessful && res.body() != null) {
                val remote = res.body()!!
                val local = dao.getDiseaseById(id)
                val merged = remote.copy(
                    // تعاملات محلی را حفظ کن تا هنگام ورود به حساب، علائم شخصی از بین نرود
                    is_favorite = (remote.is_favorite == true) || (local?.is_favorite == true),
                    is_bookmarked = (remote.is_bookmarked == true) || (local?.is_bookmarked == true),
                    is_learned = (remote.is_learned == true) || (local?.is_learned == true),
                    personal_note = prefs.getLocalNote(id).takeIf { it.isNotBlank() }
                        ?: remote.personal_note?.takeIf { it.isNotBlank() }
                        ?: (local?.personal_note ?: "")
                )
                dao.insert(DiseaseEntity.fromDomain(merged))
                Result.success(merged)
            } else {
                val local = dao.getDiseaseById(id)
                if (local != null) Result.success(local.toDomain())
                else Result.failure(Exception("پرونده در دستگاه موجود نیست"))
            }
        } catch (e: Exception) {
            val local = dao.getDiseaseById(id)
            if (local != null) Result.success(local.toDomain())
            else Result.failure(Exception("خطا در دریافت پرونده: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** فهرست مرجع مسیرهای انتقال و میزبان‌ها با شمارش پوشش (GET /api/taxonomy-refs). */
    suspend fun loadTaxonomyRefs(): Result<TaxonomyRefs> = withContext(Dispatchers.IO) {
        try {
            val res = api.getTaxonomyRefs()
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("خطا در دریافت فهرست مرجع"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun interact(id: Int, action: String, value: Any): Result<Unit> = withContext(Dispatchers.IO) {
        // Optimistic local update
        when (action) {
            "favorite" -> dao.updateFavorite(id, value as? Boolean ?: false)
            "bookmark" -> dao.updateBookmark(id, value as? Boolean ?: false)
            "learned" -> dao.updateLearned(id, value as? Boolean ?: false)
            "note" -> {
                val noteStr = value as? String ?: ""
                dao.updateNote(id, noteStr)
                prefs.saveLocalNote(id, noteStr)
            }
        }

        val token = prefs.token
        if (!token.isNullOrBlank()) {
            try {
                val json = JSONObject().apply {
                    put("action", action)
                    put("value", value)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                api.interact(id, "Bearer $token", body)
                if (action == "note") {
                    // همگام‌سازی موفق یعنی متن دیگر «در انتظار ارسال» نیست
                    if (prefs.getLocalNote(id) == value) prefs.saveLocalNote(id, "")
                }
            } catch (e: Exception) {
                // Keep local changes even if cloud sync times out
            }
        }
        Result.success(Unit)
    }

    suspend fun login(username: String, pass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(username.trim(), pass))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.token?.let { prefs.token = it }
                auth.username?.let { prefs.username = it }
                auth.role?.let { prefs.role = it }
                // Reload diseases to fetch personal sync states
                loadDiseases()
                Result.success(auth)
            } else {
                val err = response.errorBody()?.string() ?: "نام کاربری یا رمز عبور اشتباه است"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    suspend fun register(username: String, pass: String, email: String? = null): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(RegisterRequest(username.trim(), pass, email?.trim()?.takeIf { it.isNotEmpty() }))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.token?.let { prefs.token = it }
                auth.username?.let { prefs.username = it }
                auth.role?.let { prefs.role = it }
                loadDiseases()
                Result.success(auth)
            } else {
                val err = response.errorBody()?.string() ?: "خطا در ثبت‌نام"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /**
     * ورود با گوگل.
     *
     * کار کتابخانه‌ی گوگل فقط صدور id_token است؛ اعتبارسنجی واقعی (امضا، مخاطب،
     * ایمیل تأییدشده) همان‌جا که راز سرور است یعنی در pathodex.ir انجام می‌شود.
     */
    suspend fun googleLogin(idToken: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.token?.let { prefs.token = it }
                auth.username?.let { prefs.username = it }
                auth.role?.let { prefs.role = it }
                auth.email?.let { prefs.email = it }
                loadDiseases()
                Result.success(auth)
            } else {
                val err = response.errorBody()?.string() ?: "ورود با گوگل ناموفق بود"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    suspend fun fetchCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.getMe("Bearer $token")
            if (res.isSuccessful && res.body() != null) {
                val u = res.body()!!
                prefs.role = u.role
                prefs.username = u.username
                Result.success(u)
            } else {
                Result.failure(Exception("جلسه کاربری نامعتبر است"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.updatePassword("Bearer $token", UpdatePasswordRequest(oldPass, newPass))
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = res.errorBody()?.string() ?: "رمز عبور فعلی نادرست است"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** تغییر نام‌کاربری؛ پاسخ توکن تازه دارد تا نشست قطع نشود. */
    suspend fun changeUsername(newUsername: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.changeUsername("Bearer $token", UsernameChangeRequest(newUsername.trim()))
            if (res.isSuccessful && res.body() != null) {
                val auth = res.body()!!
                auth.token?.let { prefs.token = it }
                auth.username?.let { prefs.username = it }
                Result.success(auth)
            } else {
                val err = res.errorBody()?.string() ?: "این نام کاربری قبلاً انتخاب شده است"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** ارسال کد تأیید به ایمیل حساب (با مکث ۶۰ ثانیه‌ای). */
    suspend fun sendEmailCode(email: String): Result<MessageResponse> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.sendEmailCode("Bearer $token", EmailCodeRequest(email.trim()))
            val body = res.body()
            if (res.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception(body?.error ?: "خطا در ارسال کد"))
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** تأیید ایمیل با کد یک‌بارمصرف. */
    suspend fun verifyEmail(email: String, code: String): Result<MessageResponse> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.verifyEmail("Bearer $token", VerifyEmailRequest(email.trim(), code.trim()))
            val body = res.body()
            if (res.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception(body?.error ?: "کد تأیید نامعتبر است"))
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** بازنشاندن رمز با کد بازیابی، بدون نیاز به رمز فعلی. */
    suspend fun resetPassword(email: String, code: String, newPass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val token = prefs.token ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val res = api.resetPassword("Bearer $token", ResetPasswordRequest(email.trim(), code.trim(), newPass))
            if (res.isSuccessful && res.body() != null) {
                val auth = res.body()!!
                auth.token?.let { prefs.token = it }
                Result.success(auth)
            } else {
                val err = res.errorBody()?.string() ?: "کد بازیابی نامعتبر یا منقضی شده است"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** درخواست کد بازیابی رمز برای حساب بدون ورود. */
    suspend fun forgotPassword(email: String): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val res = api.forgotPassword(ForgotPasswordRequest(email.trim()))
            val body = res.body()
            if (res.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception(body?.error ?: "خطا در ارسال کد بازیابی"))
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    /** مرحله ۲ بازیابی رمز بدون ورود (مسیر عمومی /api/auth/reset-password). */
    suspend fun resetPasswordPublic(email: String, code: String, newPass: String): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val res = api.publicResetPassword(ResetPasswordRequest(email.trim(), code.trim(), newPass))
            val body = res.body()
            if (res.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception(body?.error ?: "کد بازیابی نامعتبر یا منقضی شده است"))
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    fun logout() {
        prefs.clearAuth()
    }
}
