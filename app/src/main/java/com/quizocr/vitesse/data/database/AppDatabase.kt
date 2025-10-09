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
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/23.jpg",
                    firstName = "Sophie",
                    lastName = "Martin",
                    phoneNumber = "0612345678",
                    email = "sophie.martin@example.com",
                    dateOfBirth = LocalDate.of(1992, 3, 12),
                    salaryEuros = 55000.0,
                    notes = "Développeuse full-stack avec 8 ans d'expérience. Spécialisée en Kotlin et React. A travaillé sur plusieurs projets d'envergure dans le secteur bancaire. Excellentes compétences en architecture logicielle et méthodologies agiles. Recherche un poste avec plus de responsabilités techniques.",
                    isFavorite = true
                )
            )

            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/42.jpg",
                    firstName = "Lucas",
                    lastName = "Dubois",
                    phoneNumber = "0698765432",
                    email = "lucas.dubois@example.com",
                    dateOfBirth = LocalDate.of(2000, 11, 28),
                    salaryEuros = 32000.0,
                    notes = "Jeune designer UI/UX fraîchement diplômé d'une école de design. Portfolio impressionnant avec plusieurs projets personnels. Maîtrise Figma, Adobe XD et les principes de Material Design. Très motivé et créatif, cherche sa première expérience professionnelle.",
                    isFavorite = false
                )
            )

            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/31.jpg",
                    firstName = "Thomas",
                    lastName = "Bernard",
                    phoneNumber = "0687452310",
                    email = "thomas.bernard@example.com",
                    dateOfBirth = LocalDate.of(1995, 9, 5),
                    salaryEuros = 45000.0,
                    notes = "Développeur backend spécialisé en Java et Spring Boot. 5 ans d'expérience dans le développement d'APIs REST et microservices. Connaissances solides en Docker, Kubernetes et CI/CD. A participé à la migration d'une architecture monolithique vers des microservices. Préavis de 3 mois.",
                    isFavorite = false
                )
            )
            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/8.jpg",
                    firstName = "Marie",
                    lastName = "Lefebvre",
                    phoneNumber = "0645231789",
                    email = "marie.lefebvre@example.com",
                    dateOfBirth = LocalDate.of(1985, 7, 22),
                    salaryEuros = 68000.0,
                    notes = "Chef de projet digital avec 12 ans d'expérience. Certifiée PMP et Scrum Master. A piloté des projets de transformation digitale pour des grands comptes. Excellente communication et gestion d'équipes pluridisciplinaires de 10 à 20 personnes. Disponible immédiatement.",
                    isFavorite = true
                )
            )
            candidateDao.insertCandidate(
                CandidateEntity(
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/15.jpg",
                    firstName = "Camille",
                    lastName = "Rousseau",
                    phoneNumber = "0623456789",
                    email = "camille.rousseau@example.com",
                    dateOfBirth = LocalDate.of(1993, 1, 18),
                    salaryEuros = 42000.0,
                    notes = "Data analyst avec une forte appétence pour le machine learning. Master en data science. Expérience avec Python, SQL, PowerBI et Tableau. A développé des modèles prédictifs pour optimiser les campagnes marketing. Bilingue français-anglais. Peut commencer dans 1 mois.",
                    isFavorite = true
                )
            )
        }
    }
}