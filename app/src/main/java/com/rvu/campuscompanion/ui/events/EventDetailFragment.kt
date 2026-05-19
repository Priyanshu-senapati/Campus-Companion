package com.rvu.campuscompanion.ui.events

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.databinding.FragmentEventDetailBinding
import com.rvu.campuscompanion.utils.toFormattedDateTime
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.EventsViewModel
import com.rvu.campuscompanion.workers.EventReminderWorker
import java.util.concurrent.TimeUnit

class EventDetailFragment : Fragment() {
    private var _b: FragmentEventDetailBinding? = null
    private val b get() = _b!!
    private val vm: EventsViewModel by viewModels()
    private var timer: CountDownTimer? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentEventDetailBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val id = requireArguments().getString("eventId") ?: return
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        vm.loadEvent(id)
        vm.event.observe(viewLifecycleOwner) { e ->
            if (e == null) { context?.toast("Event not found"); return@observe }
            b.toolbar.title = e.title
            b.tvTitle.text = e.title
            b.tvVenue.text = e.venue
            b.tvOrganizer.text = "Organized by ${e.organizer}"
            b.tvCategory.text = e.category
            b.tvDescription.text = e.description
            b.tvDate.text = e.date.toFormattedDateTime()
            b.tvAttendeeCount.text = "${e.registeredUsers.size} students going"
            if (e.posterUrl.isNotEmpty()) {
                Glide.with(this).load(e.posterUrl).into(b.ivPoster)
            }

            val uid = FirebaseSource.currentUserId
            val isRegistered = uid != null && e.registeredUsers.contains(uid)
            b.btnRegister.text = if (isRegistered) getString(R.string.unregister_event)
                                 else getString(R.string.register_event)
            b.btnRegister.setOnClickListener {
                if (uid == null) { context?.toast("Sign in required"); return@setOnClickListener }
                if (isRegistered) vm.unregister(e.id, uid)
                else {
                    vm.register(e.id, uid)
                    scheduleReminder(e.title, e.venue, e.date)
                }
            }

            startCountdown(e.date)
        }

        b.btnShare.setOnClickListener {
            val ev = vm.event.value ?: return@setOnClickListener
            val text = "Join me at ${ev.title} on ${ev.date.toFormattedDateTime()} at ${ev.venue}"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
            }, "Share event"))
        }

        vm.actionResult.observe(viewLifecycleOwner) { res ->
            res.fold(
                onSuccess = { context?.toast("Done"); vm.loadEvent(id) },
                onFailure = { context?.toast(it.message ?: "Failed") }
            )
        }
    }

    private fun startCountdown(target: Long) {
        timer?.cancel()
        val remaining = target - System.currentTimeMillis()
        if (remaining <= 0) { b.tvCountdown.text = "Event started"; return }
        timer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(ms: Long) {
                val d = ms / 86_400_000
                val h = (ms / 3_600_000) % 24
                val m = (ms / 60_000) % 60
                val s = (ms / 1000) % 60
                b.tvCountdown.text = "Starts in %dd %02dh %02dm %02ds".format(d, h, m, s)
            }
            override fun onFinish() { b.tvCountdown.text = "Event live" }
        }.start()
    }

    private fun scheduleReminder(title: String, venue: String, eventTime: Long) {
        val delay = eventTime - System.currentTimeMillis() - 3_600_000L
        if (delay <= 0) return
        val req = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder()
                .putString("title", title).putString("venue", venue).build())
            .build()
        WorkManager.getInstance(requireContext()).enqueue(req)
    }

    override fun onDestroyView() { super.onDestroyView(); timer?.cancel(); _b = null }
}
