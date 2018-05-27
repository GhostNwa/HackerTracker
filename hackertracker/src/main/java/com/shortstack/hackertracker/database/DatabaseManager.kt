package com.shortstack.hackertracker.database

import android.content.Context
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.App
import com.shortstack.hackertracker.Constants
import com.shortstack.hackertracker.Event.ChangeConEvent
import com.shortstack.hackertracker.event.BusProvider
import com.shortstack.hackertracker.models.*
import com.shortstack.hackertracker.network.FullResponse
import com.shortstack.hackertracker.network.SyncResponse
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Chris on 3/31/2018.
 */
class DatabaseManager(context: Context) {

    val db: MyRoomDatabase = MyRoomDatabase.buildDatabase(context)

    var con : Conference? = null
        private set

    @Deprecated("Do not use, use the conferences actual title.")
    val databaseName: String = Constants.DEFCON_DATABASE_NAME

    init {

    }

    fun changeConference(con: Conference) {
        val current = db.conferenceDao().getCurrentCon()

        current.isSelected = false
        Logger.d("Updating current: " + current.index + " " + current.isSelected)
        db.conferenceDao().update(current)


        con.isSelected = true
        Logger.d("Updating con: " + con.index + " " + con.isSelected)
        db.conferenceDao().update(con)

        db.currentConference = con

        this.con = con
    }

    fun getCons(): Single<List<Conference>> {
        return db.conferenceDao().getAll()
    }

//    fun getRecentUpdates(): Flowable<List<Event>> {
//        return db.eventDao().getRecentlyUpdated(db.currentConference?.directory
//                ?: return db.eventDao().getRecentlyUpdated())
//    }

    fun getSchedule(): Flowable<List<Event>> {
//        return db.eventDao().getFullSchedule(db.currentConference?.directory

//        ?:
        return db.eventDao().getFullSchedule()
//        )

    }

    fun getTypes(): Single<List<Type>> {
        return db.typeDao().getTypes(db.currentConference?.directory
                ?: return db.typeDao().getTypes())
    }

    fun changeConference(con: Int) {
        db.conferenceDao().getCon(con).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    changeConference(it)
                    BusProvider.bus.post(ChangeConEvent())
                }, {

                })
    }

    fun getVendors(): Flowable<List<Vendor>> {
        return db.vendorDao().getAll(db.currentConference?.directory
                ?: return db.vendorDao().getAll())
    }

    fun getCurrentCon(): Conference {
        val currentCon = db.conferenceDao().getCurrentCon()
        db.currentConference = currentCon
        return currentCon
    }

    fun getEventTypes() : Flowable<List<DatabaseEvent>> {
        return db.eventDao().getEventTypes(db.currentConference!!.directory, App.getCurrentDate())
    }

    fun getRecent() : Flowable<List<Event>> {
        return db.eventDao().getRecentlyUpdated(db.currentConference!!.directory)
    }

    fun updateConference(body: SyncResponse) : Single<Int> {
        db.eventDao().update(body.events)
        return Single.fromCallable { 0 }
    }

    fun updateConference(response: FullResponse) : Single<Int> {
        return updateConference(response.syncResponse)
    }

    fun findItem(id: Int): Flowable<Event> {
        return db.eventDao().getEventById(id)
    }
}