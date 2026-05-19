package com.rvu.campuscompanion.ui.timetable

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rvu.campuscompanion.data.local.TimetableEntry
import com.rvu.campuscompanion.databinding.SheetAddClassBinding
import com.rvu.campuscompanion.utils.toast

class AddClassBottomSheet(
    private val onAdd: (TimetableEntry) -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetAddClassBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SheetAddClassBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.actDay.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, TimetableEntry.DAYS))
        b.actType.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            listOf(TimetableEntry.TYPE_LECTURE, TimetableEntry.TYPE_LAB, TimetableEntry.TYPE_TUTORIAL)))

        b.etStart.setOnClickListener { pickTime(b.etStart) }
        b.etEnd.setOnClickListener { pickTime(b.etEnd) }

        b.btnAdd.setOnClickListener {
            val subj = b.etSubject.text.toString().trim()
            val prof = b.etProfessor.text.toString().trim()
            val room = b.etRoom.text.toString().trim()
            val day = b.actDay.text.toString().trim()
            val type = b.actType.text.toString().trim().ifBlank { TimetableEntry.TYPE_LECTURE }
            val start = b.etStart.text.toString().trim()
            val end = b.etEnd.text.toString().trim()

            if (subj.isBlank() || prof.isBlank() || room.isBlank() ||
                day.isBlank() || start.isBlank() || end.isBlank()) {
                context?.toast("Please fill all fields"); return@setOnClickListener
            }
            onAdd(TimetableEntry(0, subj, prof, room, day, start, end, type, 5, "CSE"))
            dismiss()
        }
    }

    private fun pickTime(field: android.widget.EditText) {
        val now = java.util.Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, m ->
            field.setText("%02d:%02d".format(h, m))
        }, now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE), true).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
