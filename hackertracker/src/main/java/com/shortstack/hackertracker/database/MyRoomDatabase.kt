package com.shortstack.hackertracker.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.App
import com.shortstack.hackertracker.events.SetupDatabaseEvent
import com.shortstack.hackertracker.fromFile
import com.shortstack.hackertracker.models.*
import com.shortstack.hackertracker.models.response.Speakers
import com.shortstack.hackertracker.models.response.Types
import com.shortstack.hackertracker.models.response.Vendors
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Chris on 3/31/2018.
 */
@Database(entities = [(Conference::class), (Event::class), (Type::class), (Vendor::class), (Speaker::class), (FAQ::class)], version = 1)
@TypeConverters(value = [(Converters::class)])
abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun conferenceDao(): ConferenceDao

    abstract fun eventDao(): EventDao

    abstract fun typeDao(): TypeDao

    abstract fun speakerDao(): SpeakerDao

    abstract fun vendorDao(): VendorDao

    abstract fun faqDao(): FAQDao

    var initialized: Boolean = true

    @Inject
    lateinit var gson: Gson

    fun init() {
        App.application.myComponent.inject(this)

        val conferences = gson.fromFile<Conferences>(CONFERENCES_FILE, root = "conferences")

        conferences.let {
            it.conferences.first().isSelected = true
            conferenceDao().insertAll(it.conferences)
        }

        conferences.conferences.forEach {
            val database = it.directory

            // Types
            gson.fromFile<Types>(TYPES_FILE, root = database).let {
                it.types.forEach { it.con = database }
                typeDao().insertAll(it.types)
            }

            // Schedule
            gson.fromFile<Events>(SCHEDULE_FILE, root = database).let {
                it.events.forEach { it.con = database }
                eventDao().insertAll(it.events)
            }

            // Vendors
            gson.fromFile<Vendors>(VENDORS_FILE, root = database).let {
                it.vendors.forEach { it.con = database }
                vendorDao().insertAll(it.vendors)
            }

            // Speakers
            gson.fromFile<Speakers>(SPEAKERS_FILE, root = database).let {
                it.speakers.forEach { it.con = database }
                speakerDao().insertAll(it.speakers)
            }

            gson.fromFile<FAQs>(FAQ_FILE, root = database).let {
                faqDao().insertAll(it.faqs)
            }
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getInstance(context: Context): MyRoomDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }


        fun buildDatabase(context: Context): MyRoomDatabase {
            Logger.d("Creating database! " + (System.currentTimeMillis() - App.application.timeToLaunch))
            val database = Room.databaseBuilder(context, MyRoomDatabase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            Logger.d("Database onCreate! " + (System.currentTimeMillis() - App.application.timeToLaunch))
                            super.onCreate(db)
//                            Logger.d("Database onCreate!")
                            getInstance(context).initialized = false

                            Single.fromCallable {
                                getInstance(context).init()

//                                Logger.e("Database now initialized -- firing event.")

                                getInstance(context).initialized = true
                                App.application.postBusEvent(SetupDatabaseEvent())
                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe()
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Logger.d("Database onOpen! " + (System.currentTimeMillis() - App.application.timeToLaunch))
//                            Logger.d("Database onOpen!")
//                            Logger.e("Database already initialized.")
                        }
                    }).build()

            INSTANCE = database

            return database

        }

        private const val DATABASE_NAME = "database"

        private const val CONFERENCES_FILE = "conferences.json"

        private const val SCHEDULE_FILE = "schedule-full.json"
        private const val TYPES_FILE = "event_type.json"
        private const val SPEAKERS_FILE = "speakers.json"
        private const val VENDORS_FILE = "vendors.json"
        private const val FAQ_FILE = "faq.json"
    }
}