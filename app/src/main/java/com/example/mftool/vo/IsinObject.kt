package com.example.mftool.vo

data class IsinObject(
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
