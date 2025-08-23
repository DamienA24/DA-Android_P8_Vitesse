package com.quizocr.vitesse.data.database

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.data.entity.CandidateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.candidateDao())
                }
            }
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

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun populateDatabase(candidateDao: CandidateDao) {
            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "", // ou votre URI
                    firstName = "John",
                    lastName = "Doe",
                    phoneNumber = "1234567890",
                    email = "john.c.calhoun@examplepetstore.com",
                    dateOfBirth = LocalDate.of(1998, 5, 15),
                    salaryEuros = 50000.0,
                    notes = "",
                    isFavorite = true,
                    )
            )

            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "", // ou votre URI
                    firstName = "John",
                    lastName = "Wick",
                    phoneNumber = "1234567890",
                    email = "john.c.calhoun@examplepetstore.com",
                    dateOfBirth = LocalDate.of(1998, 5, 15),
                    salaryEuros = 50000.0,
                    notes = "",
                    isFavorite = false,
                )
            )
        }
    }


}
