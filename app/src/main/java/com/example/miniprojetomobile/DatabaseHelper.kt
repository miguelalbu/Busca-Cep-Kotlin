package com.example.miniprojetomobile

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CepDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "cep_data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_JSON = "json_data"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_JSON TEXT
            )
        """
        db?.execSQL(createTableQuery)
    }



    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertJsonData(jsonData: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_JSON, jsonData)
        }
        return db.insert(TABLE_NAME, null, values).also {
            db.close()
        }
    }

    fun getAllJsonData(): List<String> {
        val db = this.readableDatabase
        val jsonList = mutableListOf<String>()
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_JSON), null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                jsonList.add(getString(getColumnIndexOrThrow(COLUMN_JSON)))
            }
            close()
        }
        db.close()
        return jsonList
    }
}
