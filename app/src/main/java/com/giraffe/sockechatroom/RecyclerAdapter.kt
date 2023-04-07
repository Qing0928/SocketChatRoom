package com.giraffe.sockechatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class RecyclerAdapter(private val record:MutableList<Msg>):RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val sendBy: TextView = v.findViewById(R.id.textSendBy)
        val content: TextView = v.findViewById(R.id.textContent)
        val time:TextView = v.findViewById(R.id.textTime)
    }

    fun setRecord(newRecord: List<Msg>){
        val diffResult = DiffUtil.calculateDiff(MsgDiffUtilCallback(record, newRecord))
        record.clear()
        record.addAll(newRecord)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_msg_template, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return record.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msgContent = record[position].content
        holder.content.text = msgContent
        val msgSendBy = record[position].sendby + "-"
        holder.sendBy.text = msgSendBy
        val msgTimeUnix = record[position].time
        val instant = Instant.ofEpochSecond(msgTimeUnix.toLong())
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val msgTime = localDateTime.toString().replace("T", " ")
        holder.time.text = msgTime
    }
}

class MsgDiffUtilCallback(private val oldList: List<Msg>, private val newList: List<Msg>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].time == newList[newItemPosition].time
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].time == newList[newItemPosition].time -> true
            oldList[oldItemPosition].content == newList[newItemPosition].content -> true
            else -> false
        }
    }
}