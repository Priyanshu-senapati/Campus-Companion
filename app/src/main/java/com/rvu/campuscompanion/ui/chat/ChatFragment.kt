package com.rvu.campuscompanion.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.adapter.ChatGroupAdapter
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.data.repository.AuthRepository
import com.rvu.campuscompanion.databinding.FragmentChatBinding
import com.rvu.campuscompanion.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment() {
    private var _b: FragmentChatBinding? = null
    private val b get() = _b!!
    private val vm: ChatViewModel by viewModels()
    private val adapter = ChatGroupAdapter { g ->
        val args = Bundle().apply { putString("groupId", g.id); putString("groupName", g.name) }
        findNavController().navigate(R.id.action_chat_to_group, args)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentChatBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter

        val uid = FirebaseSource.currentUserId
        if (uid == null) { vm.loadGroups("CSE", 5); return }

        CoroutineScope(Dispatchers.IO).launch {
            val user = AuthRepository().fetchUser(uid)
            withContext(Dispatchers.Main) {
                vm.loadGroups(user?.branch?.ifBlank { "CSE" } ?: "CSE",
                              user?.semester?.takeIf { it > 0 } ?: 5)
            }
        }
        vm.groups.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
