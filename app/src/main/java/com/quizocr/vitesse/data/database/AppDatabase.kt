package com.quizocr.vitesse.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.data.entity.CandidateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [CandidateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun candidateDao(): CandidateDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: com.quizocr.vitesse.data.database.AppDatabase? = null


        fun getDatabase(context: Context, coroutineScope: CoroutineScope): com.quizocr.vitesse.data.database.AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    com.quizocr.vitesse.data.database.AppDatabase::class.java,
                    "VitesseDB"
                )
                    .addCallback(AppDatabaseCallback(coroutineScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
