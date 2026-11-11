package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** آیتم فهرست مرجع مسیرهای انتقال (با شمارش پرونده‌هایی که آن مسیر را دارند). */
@JsonClass(generateAdapter = true)
data class TransmissionRouteRef(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "description_fa") val description_fa: String? = null,
    @Json(name = "count") val count: Int = 0
)

/** آیتم فهرست مرجع گروه‌های میزبان. */
@JsonClass(generateAdapter = true)
data class HostGroupRef(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "count") val count: Int = 0
)

/** تعریف نقش میزبان (مخزن، میزبان، ناقل و ...). */
@JsonClass(generateAdapter = true)
data class HostRoleRef(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "description_fa") val description_fa: String? = null
)

/** تعریف سطح اهمیت مسیر انتقال (اصلی، ثانویه، نادر). */
@JsonClass(generateAdapter = true)
data class TransmissionLevelRef(
    @Json(name = "slug") val slug: String,
    @Json(name = "name_fa") val name_fa: String? = null,
    @Json(name = "name_en") val name_en: String? = null,
    @Json(name = "description_fa") val description_fa: String? = null
)

@JsonClass(generateAdapter = true)
data class HostScopeCoverage(
    @Json(name = "human") val human: Int = 0,
    @Json(name = "non_human") val non_human: Int = 0,
    @Json(name = "both") val both: Int = 0,
    @Json(name = "unknown") val unknown: Int = 0
)

@JsonClass(generateAdapter = true)
data class TaxonomyCoverage(
    @Json(name = "total_diseases") val total_diseases: Int = 0,
    @Json(name = "with_transmission") val with_transmission: Int = 0,
    @Json(name = "with_hosts") val with_hosts: Int = 0,
    @Json(name = "host_scope") val host_scope: HostScopeCoverage? = null
)

/**
 * پاسخ GET /api/taxonomy-refs — فهرست مرجع مسیرهای انتقال و میزبان‌ها
 * به‌همراه شمارش پوشش در پرونده‌ها.
 */
@JsonClass(generateAdapter = true)
data class TaxonomyRefs(
    @Json(name = "transmission_routes") val transmission_routes: List<TransmissionRouteRef> = emptyList(),
    @Json(name = "host_groups") val host_groups: List<HostGroupRef> = emptyList(),
    @Json(name = "host_roles") val host_roles: List<HostRoleRef> = emptyList(),
    @Json(name = "transmission_levels") val transmission_levels: List<TransmissionLevelRef> = emptyList(),
    @Json(name = "coverage") val coverage: TaxonomyCoverage? = null
)
