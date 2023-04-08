# SocketChatRoom
網路最佳化作業

## 起始畫面

### 確認使用者是否有使用過

```
 val pref = getSharedPreferences("nick_name", 0)
 val editor = pref.edit()
 var nickName = pref.getString("nick_name", "")
 val intent = Intent("android.intent.action.MAIN")
 intent.addCategory("android.intent.category.Chat")

 //檢查是否已經有輸入暱稱
 if (!nickName.isNullOrEmpty()){
  startActivity(intent)//有則直接跳轉
 }
```

### 第一次使用

```
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
```

### 下一頁按鈕

```
binding.buttonNext.setOnClickListener {
            try {
                nickName = binding.editTextNickName.text.toString()
                if(nickName!!.isEmpty()){
                    runOnUiThread{
                        Toast.makeText(this, "請填寫暱稱", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if (db.loadMsg().isEmpty()){
                        Toast.makeText(this, "等待中", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        editor.putString("nick_name", nickName).apply()
                        startActivity(intent)
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
```

## 聊天畫面 

### Thread-1 發送訊息

```
Thread {
            try {
                val client = Socket("34.168.67.230", 8051)
                client.keepAlive = true
                val output = PrintWriter(client.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(client.getInputStream()))
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
```

### Thread-2 定期更聊天內容

```
Thread{
            try{
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
            }catch (e:Exception){
                e.printStackTrace()
            }

        }.start()
```
