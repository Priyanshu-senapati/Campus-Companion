package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.data.model.ChatGroup
import com.rvu.campuscompanion.databinding.ItemChatGroupBinding

class ChatGroupAdapter(
    private val onClick: (ChatGroup) -> Unit
) : ListAdapter<ChatGroup, ChatGroupAdapter.VH>(Diff) {

    inner class VH(val b: ItemChatGroupBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(g: ChatGroup) {
            b.tvName.text = g.name
            b.tvSubtitle.text = if (g.branch == "ALL") "RVU community" else "${g.branch} • Sem ${g.semester}"
            b.tvInitial.text = g.name.firstOrNull()?.uppercase() ?: "?"
            b.root.setOnClickListener { onClick(g) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(ItemChatGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<ChatGroup>() {
        override fun areItemsTheSame(a: ChatGroup, b: ChatGroup) = a.id == b.id
        override fun areContentsTheSame(a: ChatGroup, b: ChatGroup) = a == b
    }
}
