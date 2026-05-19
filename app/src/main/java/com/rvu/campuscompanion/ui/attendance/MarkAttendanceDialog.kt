package com.rvu.campuscompanion.ui.attendance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rvu.campuscompanion.data.local.AttendanceEntity
import com.rvu.campuscompanion.databinding.SheetMarkAttendanceBinding
import com.rvu.campuscompanion.utils.toFormattedDate
import com.rvu.campuscompanion.utils.toast
import java.util.Calendar

class MarkAttendanceDialog(
    private val onMark: (AttendanceEntity) -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetMarkAttendanceBinding? = null
    private val b get() = _b!!
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SheetMarkAttendanceBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val defaults = listOf(
            "Operating Systems", "Database Management", "Computer Networks",
            "Theory of Computation", "Software Engineering"
        )
        b.actSubject.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, defaults))
        b.tvDate.text = selectedDate.toFormattedDate()
        b.tvDate.setOnClickListener { pickDate() }

        b.btnMark.setOnClickListener {
            val subject = b.actSubject.text.toString().trim()
            if (subject.isBlank()) { context?.toast("Choose a subject"); return@setOnClickListener }
            val status = if (b.toggleStatus.checkedButtonId == b.btnPresent.id)
                AttendanceEntity.PRESENT else AttendanceEntity.ABSENT
            onMark(AttendanceEntity(0, subject, selectedDate, status, 5))
            dismiss()
        }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d); selectedDate = cal.timeInMillis
            b.tvDate.text = selectedDate.toFormattedDate()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
