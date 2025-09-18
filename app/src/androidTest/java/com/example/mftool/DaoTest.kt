package com.example.mftool

import com.example.mftool.db.IsinEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class DaoTest : DatabaseTest() {

    @Test
    fun getIsin() = runTest {
        isinDao.insert(
            arrayListOf(
                IsinEntity(
                    schemeCode = 122639,
                    nav = 92.67110,
                    date = "26-06-2025",
                    fundHouse = "PPFAS Mutual Fund",
                    schemeName = "PPFAS Fund",
                    schemeType = "Open Ended Schemes",
                    schemeCategory = "Equity Scheme - Sectoral/ Thematic",
                    peak = 0.0,
                    peakDate = "28-09-2024"
                )
            )
        )

        val isin = isinDao.getIsinEntitiesSync().first()

        assertEquals(
            92.67110,
            isin.nav,
        )
    }

    @Test
    fun updateNavAndPeak() = runTest {
        isinDao.insert(
            arrayListOf(
                IsinEntity(
                    schemeCode = 122639,
                    nav = 92.67110,
                    date = "26-06-2025",
                    fundHouse = "PPFAS Mutual Fund",
                    schemeName = "PPFAS Fund",
                    schemeType = "Open Ended Schemes",
                    schemeCategory = "Equity Scheme - Sectoral/ Thematic",
                    peak = 92.67110,
                    peakDate = "28-09-2024"
                )
            )
        )

        isinDao.upsertNav(
            IsinEntity(
                schemeCode = 122639,
                nav = 93.16600,
                date = "27-06-2025",
                fundHouse = "PPFAS Mutual Fund",
                schemeName = "PPFAS Fund",
                schemeType = "Open Ended Schemes",
                schemeCategory = "Equity Scheme - Sectoral/ Thematic",
                peak = maxOf(93.16600, isinDao.getPeak(122639)),
                peakDate = "28-09-2024"
            )
        )

        val isin = isinDao.getIsinEntitiesSync().first()

        assertEquals(
            93.16600,
            isin.nav,
        )

        assertEquals(
            "27-06-2025",
            isin.date,
        )

        assertEquals(
            93.16600,
            isin.peak,
        )
    }
}