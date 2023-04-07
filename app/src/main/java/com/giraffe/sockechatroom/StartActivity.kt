package com.giraffe.sockechatroom

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.giraffe.sockechatroom.databinding.ActivityStartBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

@SuppressLint("CommitPrefEdits")
class StartActivity : AppCompatActivity() {
    private val db = RecordDB(this, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pref = getSharedPreferences("nick_name", 0)
        val editor = pref.edit()
        var nickName = pref.getString("nick_name", "")
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.Chat")

        //檢查是否已經有輸入暱稱
        if (!nickName.isNullOrEmpty()){
            startActivity(intent)//有則直接跳轉
        }
        //沒有則啟動Thread初始化
        else{
            Thread{
                try {
                    val client = Socket("34.168.67.230", 8051)
                    client.keepAlive = true
                    val output = PrintWriter(client.getOutputStream(), true)
                    val input = BufferedReader(InputStreamReader(client.getInputStream()))
                    output.println("first")
                    val receiveMsg = JSONArray(input.readLine())
                    println(receiveMsg)
                    for (i in 0 until receiveMsg.length()){
                        val msg = JSONObject(receiveMsg[i].toString())
                        val time = msg.getInt("time")
                        val sendby = msg.getString("sendby")
                        val content = msg.getString("content")
                        val recordMsg = Msg(time, sendby, content)
                        val check = db.checkMsg(time)
                        if (check) db.newMsg(recordMsg)
                    }
                    client.close()
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }.start()
        }


        //下一步按鈕
        binding.buttonNext.setOnClickListener {
            try {
                nickName = binding.editTextNickName.text.toString()
                if(nickName!!.isEmpty()){
                    runOnUiThread{
                        Toast.makeText(this, "請填寫暱稱", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    editor.putString("nick_name", nickName).apply()
                    startActivity(intent)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}