package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseDao {
    @Query("SELECT * FROM diseases ORDER BY id DESC")
    fun getAllDiseases(): Flow<List<DiseaseEntity>>

    @Query("SELECT * FROM diseases ORDER BY id DESC")
    suspend fun getAllDiseasesSync(): List<DiseaseEntity>

    @Query("SELECT * FROM diseases WHERE id = :id LIMIT 1")
    suspend fun getDiseaseById(id: Int): DiseaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diseases: List<DiseaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(disease: DiseaseEntity)

    @Query("UPDATE diseases SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE diseases SET is_bookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Int, isBookmarked: Boolean)

    @Query("UPDATE diseases SET is_learned = :isLearned WHERE id = :id")
    suspend fun updateLearned(id: Int, isLearned: Boolean)

    @Query("UPDATE diseases SET personal_note = :note WHERE id = :id")
    suspend fun updateNote(id: Int, note: String)

    @Query("DELETE FROM diseases")
    suspend fun clearAll()

    @Query("DELETE FROM diseases WHERE id NOT IN (:serverIds)")
    suspend fun deleteNotIn(serverIds: List<Int>)
}
