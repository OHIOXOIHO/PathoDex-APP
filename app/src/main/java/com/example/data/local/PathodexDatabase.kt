package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// نسخه ۲: افزودن بخش‌های بالینی، مسیرهای انتقال، میزبان‌ها و سرده به پرونده.
// fallbackToDestructiveMigration باعث می‌شود داده‌ی قدیمی پاک و از سرور بارگذاری شود.
@Database(entities = [DiseaseEntity::class], version = 2, exportSchema = false)
abstract class PathodexDatabase : RoomDatabase() {
    abstract fun diseaseDao(): DiseaseDao

    companion object {
        @Volatile
        private var INSTANCE: PathodexDatabase? = null

        fun getDatabase(context: Context): PathodexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PathodexDatabase::class.java,
                    "pathodex_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
