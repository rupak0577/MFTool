package com.example.mftool.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mftool.vo.IsinObject

@Entity(tableName = "isin")
data class IsinEntity(
    @PrimaryKey
    val schemeCode: Int,
    val date: String,
    val nav: Double,
    val fundHouse: String,
    val schemeName: String,
    val schemeType: String,
    val schemeCategory: String,
    val peak: Double,
    val peakDate: String,
    val owners: String
)

fun IsinEntity.toIsinObject() = IsinObject(
    schemeCode = this.schemeCode,
    date = this.date,
    nav = this.nav,
    fundHouse = this.fundHouse,
    schemeName = this.schemeName,
    schemeType = this.schemeType,
    schemeCategory = this.schemeCategory,
    peak = this.peak,
    peakDate = this.peakDate,
    owners = this.owners
)

