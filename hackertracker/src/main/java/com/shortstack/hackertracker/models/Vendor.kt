package com.shortstack.hackertracker.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Vendor(
        @PrimaryKey(autoGenerate = true)
        val index: Int,
        val title: String,
        val description: String,
        val link: String,
        val partner: Int = 0,
        var con: String
) : Parcelable

