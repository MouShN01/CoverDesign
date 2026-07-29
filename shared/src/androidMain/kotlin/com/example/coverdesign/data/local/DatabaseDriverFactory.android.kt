package com.example.coverdesign.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.coverdesign.db.AppDatabase


actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "app_v3.db",
            callback = object : AndroidSqliteDriver.Callback(AppDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            }
        )
//
//        AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")

}
//actual class DatabaseDriverFactory(private val context: Context) {
//    actual fun createDriver(): SqlDriver =
//        AndroidSqliteDriver(
//            schema   = AppDatabase.Schema,
//            context  = context,
//            name     = "app.db",
//            callback = object : SupportSQLiteOpenHelper.Callback(AppDatabase.Schema.version.toInt()) {
//                override fun onCreate(db: SupportSQLiteDatabase) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onUpgrade(
//                    db: SupportSQLiteDatabase,
//                    oldVersion: Int,
//                    newVersion: Int
//                ) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onOpen(db: SupportSQLiteDatabase) {
//                    super.onOpen(db)
//                    db.execSQL("PRAGMA foreign_keys = ON")
//                }
//            }
//        )
//}