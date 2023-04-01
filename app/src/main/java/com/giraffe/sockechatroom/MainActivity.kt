package com.giraffe.sockechatroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.giraffe.sockechatroom.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread {
            try {
                val client = Socket("192.168.250.123", 8051)
                client.keepAlive = true
                val output = PrintWriter(client.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(client.getInputStream()), 2048)
                binding.buttonSendMsg.setOnClickListener {
                    Thread{
                        try {
                            val msg = binding.editTextSendMsg.text.toString()
                            output.println(msg)
                            println("Receive From Server: ${input.readLine()}")
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
            val updateClient = Socket("192.168.250.123", 8051)
            updateClient.keepAlive = true
            val updateOutput = PrintWriter(updateClient.getOutputStream(), true)
            val updateInput = BufferedReader(InputStreamReader(updateClient.getInputStream()), 1024)
            Timer().schedule(object :TimerTask(){
                override fun run() {
                    try {
                        updateOutput.println("latest,2023-03-26 16:00:32")
                        println("Receive Update From Server:${updateInput.readLine()}")
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }, Date(), 1000)
        }.start()
    }
}