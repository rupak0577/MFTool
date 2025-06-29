package com.example.mftool

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.mftool.db.IsinDao
import com.example.mftool.db.IsinDb
import org.junit.After
import org.junit.Before

abstract class DatabaseTest {

    private lateinit var db: IsinDb
    protected lateinit var isinDao: IsinDao

    @Before
    fun setup() {
        db = run {
            val context = ApplicationProvider.getApplicationContext<Context>()
            Room.inMemoryDatabaseBuilder(
                context,
                IsinDb::class.java,
            ).build()
        }
        isinDao = db.isinDao()
    }

    @After
    fun teardown() = db.close()
}