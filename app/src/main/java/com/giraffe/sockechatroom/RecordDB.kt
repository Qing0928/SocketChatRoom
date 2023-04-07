package com.giraffe.sockechatroom

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

@SuppressLint("Range")
class RecordDB(
    context: Context,
    factory:SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context,DATABASE_NAME, factory, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "chatroom.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "CREATE TABLE IF NOT EXISTS record(time INTEGER, sendby TEXT DEFAULT '', content TEXT)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
    }

    fun newMsg(msg:Msg){
        val db = this.writableDatabase
        val values = ContentValues()
        try {
            values.put("time", msg.time)
            values.put("sendby", msg.sendby)
            values.put("content", msg.content)
            db.insert("record", null, values)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun checkMsg(time:Int):Boolean{
        val db = this.readableDatabase
        val sql = "SELECT `time` FROM record WHERE `time`='$time'"
        val cursor = db.rawQuery(sql, null)
        return try {
            cursor.count == 0
        }catch (e:Exception){
            e.printStackTrace()
            false
        }finally {
            cursor.close()
        }
    }

    fun loadMsg():List<Msg>{
        val db = this.readableDatabase
        val sql = "SELECT * FROM `record` ORDER BY `time` ASC"
        val cursor = db.rawQuery(sql, null)
        val chatRecord = ArrayList<Msg>()
        try {
            return if (cursor.moveToFirst()){
                do {
                    val time = cursor.getInt(cursor.getColumnIndex("time"))
                    val sendby = cursor.getString(cursor.getColumnIndex("sendby"))
                    val content = cursor.getString(cursor.getColumnIndex("content"))
                    chatRecord.add(
                        Msg(time, sendby, content)
                    )
                }while (cursor.moveToNext())
                chatRecord
            }else listOf()
        }catch (e:Exception){
            e.printStackTrace()
            return listOf()
        }finally {
            cursor.close()
        }
    }

    fun getLatest():Int{
        val db = this.readableDatabase
        val sql = "SELECT `time` FROM record ORDER BY `time` DESC LIMIT 1"
        val cursor = db.rawQuery(sql, null)
        var latest = 0
        return try {
            if (cursor.moveToFirst()){
                do {
                    latest = cursor.getInt(cursor.getColumnIndex("time"))
                }while (cursor.moveToNext())
            }
            latest
        }catch (e:Exception){
            e.printStackTrace()
            latest
        }finally {
            cursor.close()
        }
    }
}