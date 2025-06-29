package com.example.mftool.api


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IsinDetailsResponse(
    @param:Json(name = "data")
    val `data`: List<Data>,
    @param:Json(name = "meta")
    val meta: Meta,
    @param:Json(name = "status")
    val status: String
) {
    @JsonClass(generateAdapter = true)
    data class Data(
        @param:Json(name = "date")
        val date: String,
        @param:Json(name = "nav")
        val nav: String
    )

    @JsonClass(generateAdapter = true)
    data class Meta(
        @param:Json(name = "fund_house")
        val fundHouse: String,
        @param:Json(name = "scheme_code")
        val schemeCode: Int,
        @param:Json(name = "scheme_name")
        val schemeName: String,
        @param:Json(name = "scheme_type")
        val schemeType: String,
        @param:Json(name = "scheme_category")
        val schemeCategory: String
    )
}