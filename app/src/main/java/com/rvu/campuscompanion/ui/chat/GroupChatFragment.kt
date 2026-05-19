package com.rvu.campuscompanion.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rvu.campuscompanion.adapter.MessagesAdapter
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.data.repository.AuthRepository
import com.rvu.campuscompanion.databinding.FragmentGroupChatBinding
import com.rvu.campuscompanion.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatFragment : Fragment() {
    private var _b: FragmentGroupChatBinding? = null
    private val b get() = _b!!
    private val vm: ChatViewModel by viewModels()
    private var senderName = "Student"
    private val uid get() = FirebaseSource.currentUserId ?: ""

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGroupChatBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val groupId = requireArguments().getString("groupId") ?: return
        val groupName = requireArguments().getString("groupName") ?: "Chat"
        b.toolbar.title = groupName
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val adapter = MessagesAdapter(uid)
        b.rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        b.rv.adapter = adapter

        vm.openGroup(groupId)
        vm.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                if (list.isNotEmpty()) b.rv.scrollToPosition(list.size - 1)
            }
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSource.currentUserId?.let { u ->
                val user = AuthRepository().fetchUser(u)
                withContext(Dispatchers.Main) { senderName = user?.name ?: "Student" }
            }
        }

        b.btnSend.setOnClickListener {
            val text = b.etMessage.text.toString().trim()
            if (text.isBlank() || uid.isBlank()) return@setOnClickListener
            vm.send(text, uid, senderName)
            b.etMessage.text?.clear()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
