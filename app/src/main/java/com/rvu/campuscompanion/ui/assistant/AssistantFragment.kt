package com.rvu.campuscompanion.ui.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.adapter.AiChatAdapter
import com.rvu.campuscompanion.databinding.FragmentAssistantBinding
import com.rvu.campuscompanion.viewmodel.AssistantViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class AssistantFragment : Fragment() {
    private var _b: FragmentAssistantBinding? = null
    private val b get() = _b!!

    private val vm: AssistantViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(assistantRepo = app.assistantRepository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAssistantBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val adapter = AiChatAdapter()
        b.rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        b.rv.adapter = adapter

        vm.messages.observe(viewLifecycleOwner) { msgs ->
            adapter.submitList(msgs, Runnable {
                if (msgs.isNotEmpty()) b.rv.smoothScrollToPosition(msgs.size - 1)
            })
        }
        vm.isTyping.observe(viewLifecycleOwner) { typing ->
            b.tvTyping.visibility = if (typing) View.VISIBLE else View.GONE
            b.btnSend.isEnabled = !typing
            b.chipAttendance.isEnabled = !typing
            b.chipToday.isEnabled = !typing
            b.chipSkip.isEnabled = !typing
            b.chipStudy.isEnabled = !typing
        }

        b.btnSend.setOnClickListener { sendCurrent() }
        b.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendCurrent(); true } else false
        }

        b.chipAttendance.setOnClickListener { send("How is my attendance? Any subjects I should worry about?") }
        b.chipToday.setOnClickListener { send("What classes do I have today?") }
        b.chipSkip.setOnClickListener { send("Can I skip one class of OS tomorrow without falling below 75%?") }
        b.chipStudy.setOnClickListener { send("Give me 3 quick study tips for my upcoming exams.") }
    }

    private fun sendCurrent() {
        val text = b.etMessage.text?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return
        b.etMessage.setText("")
        send(text)
    }

    private fun send(text: String) {
        vm.send(text)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
