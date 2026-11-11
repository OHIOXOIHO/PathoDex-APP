package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.api.ApiClient
import com.example.data.model.Disease
import com.example.data.model.DiseaseImage
import com.example.data.model.DiseaseSection
import com.example.data.model.HostGroup
import com.example.data.model.TransmissionRoute
import com.squareup.moshi.Types

@Entity(tableName = "diseases")
@TypeConverters(Converters::class)
data class DiseaseEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val name_fa: String,
    val name_en: String?,
    val agent_fa: String,
    val agent_en: String?,
    val description_fa: String,
    val family_fa: String?,
    val family_en: String?,
    val class_fa: String?,
    val class_en: String?,
    val genus_fa: String?,
    val genus_en: String?,
    val slug: String?,
    val host_scope: String?,
    val created_at: String?,
    val updated_at: String?,
    val images: List<DiseaseImage>?,
    val sections: List<DiseaseSection>?,
    val transmission: List<TransmissionRoute>?,
    val hosts: List<HostGroup>?,
    val is_favorite: Boolean,
    val is_bookmarked: Boolean,
    val is_learned: Boolean,
    val personal_note: String
) {
    fun toDomain(): Disease {
        return Disease(
            id = id,
            category = category,
            name_fa = name_fa,
            name_en = name_en,
            agent_fa = agent_fa,
            agent_en = agent_en,
            description_fa = description_fa,
            family_fa = family_fa,
            family_en = family_en,
            class_fa = class_fa,
            class_en = class_en,
            genus_fa = genus_fa,
            genus_en = genus_en,
            slug = slug,
            host_scope = host_scope,
            created_at = created_at,
            updated_at = updated_at,
            images = images ?: emptyList(),
            sections = sections ?: emptyList(),
            transmission = transmission ?: emptyList(),
            hosts = hosts ?: emptyList(),
            is_favorite = is_favorite,
            is_bookmarked = is_bookmarked,
            is_learned = is_learned,
            personal_note = personal_note
        )
    }

    companion object {
        fun fromDomain(d: Disease): DiseaseEntity {
            return DiseaseEntity(
                id = d.id,
                category = d.category,
                name_fa = d.name_fa,
                name_en = d.name_en,
                agent_fa = d.agent_fa,
                agent_en = d.agent_en,
                description_fa = d.description_fa,
                family_fa = d.family_fa,
                family_en = d.family_en,
                class_fa = d.class_fa,
                class_en = d.class_en,
                genus_fa = d.genus_fa,
                genus_en = d.genus_en,
                slug = d.slug,
                host_scope = d.host_scope,
                created_at = d.created_at,
                updated_at = d.updated_at,
                images = d.images,
                sections = d.sections,
                transmission = d.transmission,
                hosts = d.hosts,
                is_favorite = d.is_favorite ?: false,
                is_bookmarked = d.is_bookmarked ?: false,
                is_learned = d.is_learned ?: false,
                personal_note = d.personal_note ?: ""
            )
        }
    }
}

class Converters {
    private val moshi = ApiClient.moshi
    private val imageListType = Types.newParameterizedType(List::class.java, DiseaseImage::class.java)
    private val sectionListType = Types.newParameterizedType(List::class.java, DiseaseSection::class.java)
    private val transmissionListType = Types.newParameterizedType(List::class.java, TransmissionRoute::class.java)
    private val hostListType = Types.newParameterizedType(List::class.java, HostGroup::class.java)

    private val imageAdapter = moshi.adapter<List<DiseaseImage>>(imageListType)
    private val sectionAdapter = moshi.adapter<List<DiseaseSection>>(sectionListType)
    private val transmissionAdapter = moshi.adapter<List<TransmissionRoute>>(transmissionListType)
    private val hostAdapter = moshi.adapter<List<HostGroup>>(hostListType)

    @TypeConverter
    fun fromImageList(list: List<DiseaseImage>?): String {
        return if (list == null) "[]" else imageAdapter.toJson(list)
    }

    @TypeConverter
    fun toImageList(json: String?): List<DiseaseImage>? {
        return if (json.isNullOrBlank()) emptyList() else imageAdapter.fromJson(json)
    }

    @TypeConverter
    fun fromSectionList(list: List<DiseaseSection>?): String {
        return if (list == null) "[]" else sectionAdapter.toJson(list)
    }

    @TypeConverter
    fun toSectionList(json: String?): List<DiseaseSection>? {
        return if (json.isNullOrBlank()) emptyList() else sectionAdapter.fromJson(json)
    }

    @TypeConverter
    fun fromTransmissionList(list: List<TransmissionRoute>?): String {
        return if (list == null) "[]" else transmissionAdapter.toJson(list)
    }

    @TypeConverter
    fun toTransmissionList(json: String?): List<TransmissionRoute>? {
        return if (json.isNullOrBlank()) emptyList() else transmissionAdapter.fromJson(json)
    }

    @TypeConverter
    fun fromHostList(list: List<HostGroup>?): String {
        return if (list == null) "[]" else hostAdapter.toJson(list)
    }

    @TypeConverter
    fun toHostList(json: String?): List<HostGroup>? {
        return if (json.isNullOrBlank()) emptyList() else hostAdapter.fromJson(json)
    }
}
