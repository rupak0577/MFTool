package com.example.mftool.repository

import com.opencsv.CSVReader
import java.io.Reader

fun readCsv(reader: Reader): List<Isin> {
    val csvReader = CSVReader(reader)
    val rows = csvReader.readAll()
    val iterator = rows.iterator()

    val isins = mutableListOf<Isin>()
    while (iterator.hasNext()) {
        val value = iterator.next()
        isins.add(Isin(value[0], value[1]))
    }

    csvReader.close()

    return isins
}

data class Isin(
    val name: String,
    val isin: String
)