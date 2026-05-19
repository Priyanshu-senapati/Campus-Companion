package com.rvu.campuscompanion.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {
    private var _b: FragmentMoreBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMoreBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        configureRow(b.rowTimetable.root, R.drawable.ic_schedule, "Timetable") {
            findNavController().navigate(R.id.action_more_to_timetable)
        }
        configureRow(b.rowAttendance.root, R.drawable.ic_attendance, "Attendance") {
            findNavController().navigate(R.id.action_more_to_attendance)
        }
        configureRow(b.rowLostfound.root, R.drawable.ic_lostfound, "Lost & Found") {
            findNavController().navigate(R.id.action_more_to_lostfound)
        }
        configureRow(b.rowCanteen.root, R.drawable.ic_food, "Mingo's Canteen") {
            findNavController().navigate(R.id.action_more_to_canteen)
        }
        configureRow(b.rowChat.root, R.drawable.ic_chat, "Study Groups") {
            findNavController().navigate(R.id.action_more_to_chat)
        }
    }

    private fun configureRow(root: View, iconRes: Int, label: String, onClick: () -> Unit) {
        root.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
        root.findViewById<TextView>(R.id.tvLabel).text = label
        root.setOnClickListener { onClick() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
