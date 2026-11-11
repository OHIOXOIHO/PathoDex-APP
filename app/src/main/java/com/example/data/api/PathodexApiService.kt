package com.example.data.api

import com.example.data.model.AuthResponse
import com.example.data.model.CategoriesResponse
import com.example.data.model.Disease
import com.example.data.model.EmailCodeRequest
import com.example.data.model.ForgotPasswordRequest
import com.example.data.model.GoogleLoginRequest
import com.example.data.model.LoginRequest
import com.example.data.model.MessageResponse
import com.example.data.model.RegisterRequest
import com.example.data.model.ResetPasswordRequest
import com.example.data.model.StatsResponse
import com.example.data.model.TaxonomyRefs
import com.example.data.model.UpdatePasswordRequest
import com.example.data.model.User
import com.example.data.model.UsernameChangeRequest
import com.example.data.model.VerifyEmailRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PathodexApiService {

    @GET("api/diseases")
    suspend fun getDiseases(
        @Header("Authorization") authHeader: String? = null
    ): List<Disease>

    /**
     * پرونده‌ی کامل با بخش‌های بالینی، مسیرهای انتقال و میزبان‌ها.
     * فهرست اصلی بخش‌ها را نمی‌فرستد؛ پس هنگام باز کردن پرونده همین مسیر لازم است.
     */
    @GET("api/diseases/{id}")
    suspend fun getDisease(
        @Path("id") id: Int,
        @Header("Authorization") authHeader: String? = null
    ): Response<Disease>

    @GET("api/taxonomy-refs")
    suspend fun getTaxonomyRefs(): Response<TaxonomyRefs>

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    /** ورود با گوگل: ارسال id_token به سرور برای اعتبارسنجی و صدور توکن session. */
    @POST("api/auth/google/callback")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>

    @GET("api/me")
    suspend fun getMe(
        @Header("Authorization") authHeader: String
    ): Response<User>

    @PUT("api/me/password")
    suspend fun updatePassword(
        @Header("Authorization") authHeader: String,
        @Body request: UpdatePasswordRequest
    ): Response<ResponseBody>

    /** تغییر نام‌کاربری؛ پاسخ شامل توکن تازه است تا نشست قطع نشود. */
    @POST("api/me/username")
    suspend fun changeUsername(
        @Header("Authorization") authHeader: String,
        @Body request: UsernameChangeRequest
    ): Response<AuthResponse>

    /** ارسال کد تأیید به ایمیل حساب (با مکث ۶۰ ثانیه‌ای). */
    @POST("api/me/email/send-code")
    suspend fun sendEmailCode(
        @Header("Authorization") authHeader: String,
        @Body request: EmailCodeRequest
    ): Response<MessageResponse>

    /** تأیید ایمیل با کد یک‌بارمصرف. */
    @POST("api/me/email/verify")
    suspend fun verifyEmail(
        @Header("Authorization") authHeader: String,
        @Body request: VerifyEmailRequest
    ): Response<MessageResponse>

    /** بازنشاندن رمز با کد بازیابی، بدون نیاز به رمز فعلی. */
    @POST("api/me/password/reset")
    suspend fun resetPassword(
        @Header("Authorization") authHeader: String,
        @Body request: ResetPasswordRequest
    ): Response<AuthResponse>

    /** درخواست کد بازیابی رمز برای حساب بدون ورود. */
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<MessageResponse>

    /** مرحله ۲ بازیابی رمز بدون ورود: تعیین رمز جدید با کد. */
    @POST("api/auth/reset-password")
    suspend fun publicResetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>

    @POST("api/diseases/{id}/interact")
    suspend fun interact(
        @Path("id") id: Int,
        @Header("Authorization") authHeader: String,
        @Body body: RequestBody
    ): Response<ResponseBody>

    @GET("api/v1/stats")
    suspend fun getStats(): StatsResponse

    @GET("api/v1/categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("api/v1/diseases")
    suspend fun getV1Diseases(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): Response<ResponseBody>
}
