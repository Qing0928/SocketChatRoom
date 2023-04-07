package com.giraffe.sockechatroom

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.giraffe.sockechatroom.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat", "CommitPrefEdits")
class MainActivity : AppCompatActivity() {
    private val db = RecordDB(this, null)
    private var counterCloseApp = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recycler.layoutManager = layoutManager

        val recyclerAdapter = RecyclerAdapter(db.loadMsg() as MutableList<Msg>)
        binding.recycler.adapter = recyclerAdapter
        binding.recycler.scrollToPosition(recyclerAdapter.itemCount -1 )

        val pref = getSharedPreferences("nick_name", 0)
        val nickName = pref.getString("nick_name", "")

        //發訊息Thread
        Thread {
            try {
                val client = Socket("34.168.67.230", 8051)
                client.keepAlive = true
                val output = PrintWriter(client.getOutputStream(), true)
                //發送按鈕
                binding.buttonSendMsg.setOnClickListener {
                    Thread{
                        try {
                            val service = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            service.hideSoftInputFromWindow(it.windowToken, 0)
                            val msgMap:MutableMap<String, String> = mutableMapOf()
                            msgMap["time"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                            msgMap["sendby"] = nickName!!
                            msgMap["content"] = binding.editTextSendMsg.text.toString()
                            val msg = "new," + (msgMap as Map<*, *>?)?.let { it -> JSONObject(it).toString() }
                            output.println(msg)
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

        //更新訊息Thread，500ms刷新一次
        Thread{
            val updateClient = Socket("34.168.67.230", 8051)
            updateClient.keepAlive = true
            val updateOutput = PrintWriter(updateClient.getOutputStream(), true)
            val updateInput = BufferedReader(InputStreamReader(updateClient.getInputStream()))
            Timer().schedule(object :TimerTask(){
                override fun run() {
                    try {
                        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        updateOutput.println("latest,$now")
                        if (updateInput.ready()){
                            val receiveMsg = JSONArray(updateInput.readLine())
                            for (i in 0 until receiveMsg.length()){
                                val msg = JSONObject(receiveMsg[i].toString())
                                val time = msg.getInt("time")
                                val sendby = msg.getString("sendby")
                                val content = msg.getString("content")
                                val recordMsg = Msg(time, sendby, content)
                                val check = db.checkMsg(time)
                                if (check){
                                    db.newMsg(recordMsg)
                                    runOnUiThread {
                                        val record = db.loadMsg().toMutableList()
                                        recyclerAdapter.setRecord(record)
                                        binding.recycler.scrollToPosition(recyclerAdapter.itemCount -1)
                                    }
                                }
                            }
                        }

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }, 0, 500)
        }.start()

        //按下返回執行的動作
        onBackPressedDispatcher.addCallback(this, object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                counterCloseApp += 1
                if (counterCloseApp >= 2){
                    this@MainActivity.finish()
                    finishAffinity()
                }else{
                    Toast.makeText(
                        this@MainActivity,
                        "點擊兩下關閉App",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}