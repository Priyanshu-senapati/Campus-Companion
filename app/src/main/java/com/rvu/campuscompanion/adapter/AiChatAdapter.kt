package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.model.AiChatMessage

class AiChatAdapter : ListAdapter<AiChatMessage, RecyclerView.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<AiChatMessage>() {
        override fun areItemsTheSame(o: AiChatMessage, n: AiChatMessage) = o.timestamp == n.timestamp
        override fun areContentsTheSame(o: AiChatMessage, n: AiChatMessage) = o == n
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).sender == AiChatMessage.Sender.USER) TYPE_USER else TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_USER) R.layout.item_ai_message_user else R.layout.item_ai_message_ai
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageVH(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MessageVH).bind(getItem(position))
    }

    class MessageVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvText)
        fun bind(m: AiChatMessage) { tv.text = m.text }
    }

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_AI = 1
    }
}
