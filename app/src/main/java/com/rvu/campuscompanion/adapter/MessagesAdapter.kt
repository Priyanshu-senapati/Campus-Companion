package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.data.model.ChatMessage
import com.rvu.campuscompanion.databinding.ItemMessageReceivedBinding
import com.rvu.campuscompanion.databinding.ItemMessageSentBinding
import com.rvu.campuscompanion.utils.toFormattedDateTime

class MessagesAdapter(
    private val currentUid: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(Diff) {

    private val SENT = 1
    private val RECEIVED = 2

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).senderId == currentUid) SENT else RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == SENT) {
            SentVH(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedVH(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is SentVH -> holder.bind(msg)
            is ReceivedVH -> holder.bind(msg)
        }
    }

    class SentVH(val b: ItemMessageSentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) {
            b.tvText.text = m.text
            b.tvTime.text = m.timestamp.toFormattedDateTime()
        }
    }

    class ReceivedVH(val b: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) {
            b.tvText.text = m.text
            b.tvSender.text = m.senderName.ifBlank { "Student" }
            b.tvTime.text = m.timestamp.toFormattedDateTime()
        }
    }

    object Diff : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }
}
