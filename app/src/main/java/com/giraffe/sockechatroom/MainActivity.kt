package com.giraffe.sockechatroom

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.giraffe.sockechatroom.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SuppressLint("SimpleDateFormat")
class MainActivity : AppCompatActivity() {
    private val db = RecordDB(this, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread {
            try {
                val client = Socket("192.168.67.123", 8051)
                client.keepAlive = true
                val output = PrintWriter(client.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(client.getInputStream()))

                //發送訊息
                binding.buttonSendMsg.setOnClickListener {
                    Thread{
                        try {
                            val service = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            service.hideSoftInputFromWindow(it.windowToken, 0)
                            val msgMap:MutableMap<String, String> = mutableMapOf()
                            msgMap["time"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                            msgMap["sendby"] = "giraffe0928"
                            msgMap["content"] = binding.editTextSendMsg.text.toString()
                            val msg = "new," + (msgMap as Map<*, *>?)?.let { it -> JSONObject(it).toString() }
                            output.println(msg)
                            println("Receive From Server: ${input.readLine()}")
                            binding.editTextSendMsg.text.clear()
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }.start()
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()

        Thread{
            val updateClient = Socket("192.168.67.123", 8051)
            updateClient.keepAlive = true
            val updateOutput = PrintWriter(updateClient.getOutputStream(), true)
            val updateInput = BufferedReader(InputStreamReader(updateClient.getInputStream()))
            Timer().schedule(object :TimerTask(){
                override fun run() {
                    try {
                        val latestTimeStamp = db.getLatest()
                        val instant = Instant.ofEpochSecond(latestTimeStamp.toLong())
                        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                        val latestTime = localDateTime.toString().replace("T", " ")
                        updateOutput.println("latest,$latestTime")
                        //updateOutput.println("first")
                        if (updateInput.ready()){
                            val receiveMsg = JSONArray(updateInput.readLine())
                            //println(updateInput.readLine())
                            for (i in 0 until receiveMsg.length()){
                                val msg = JSONObject(receiveMsg[i].toString())
                                val time = msg.getInt("time")
                                val sendby = msg.getString("sendby")
                                val content = msg.getString("content")
                                val recordMsg = Msg(time, sendby, content)
                                val check = db.checkMsg(time)
                                if (check) db.newMsg(recordMsg)
                                //println(receiveMsg[i])
                            }
                        }

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }, 0, 5000)
        }.start()
    }
}