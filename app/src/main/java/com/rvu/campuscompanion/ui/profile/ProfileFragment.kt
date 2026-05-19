package com.rvu.campuscompanion.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.databinding.FragmentProfileBinding
import com.rvu.campuscompanion.utils.PrefsManager
import com.rvu.campuscompanion.viewmodel.AuthViewModel
import com.rvu.campuscompanion.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: ProfileViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProfileBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val prefs = PrefsManager(requireContext())
        b.switchDark.isChecked = prefs.darkMode
        b.switchNotif.isChecked = prefs.notificationsEnabled

        vm.load()
        vm.user.observe(viewLifecycleOwner) { u ->
            if (u == null) return@observe
            b.tvName.text = u.name
            b.tvEmail.text = u.email
            b.tvUsn.text = "PRN: ${u.prn}"
            b.tvBranch.text = "${u.branch} • Semester ${u.semester}"
            b.tvBio.text = u.bio.ifBlank { "No bio yet — tap edit to add one." }
            b.tvAttendanceStat.text = "—"
            b.tvEventsStat.text = u.eventsAttended.toString()
            b.tvItemsStat.text = u.itemsPosted.toString()
            if (u.photoUrl.isNotEmpty()) {
                Glide.with(this).load(u.photoUrl)
                    .placeholder(R.drawable.ic_default_avatar).into(b.ivAvatar)
            }
            renderBadges(u.eventsAttended, u.itemsPosted)
        }

        b.btnEdit.setOnClickListener { findNavController().navigate(R.id.action_profile_to_edit) }
        b.switchDark.setOnCheckedChangeListener { _, checked ->
            prefs.darkMode = checked
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        b.switchNotif.setOnCheckedChangeListener { _, checked -> prefs.notificationsEnabled = checked }

        b.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign out?")
                .setMessage("You will need to sign in again to access the app.")
                .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                    authVm.signOut()
                    findNavController().navigate(R.id.action_profile_to_login)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun renderBadges(eventsCount: Int, itemsCount: Int) {
        b.badgeAttendee.alpha = 1f
        b.badgeEventEnthusiast.alpha = if (eventsCount >= 3) 1f else 0.3f
        b.badgeHelper.alpha = if (itemsCount >= 2) 1f else 0.3f
        b.badgeEarlyBird.alpha = 0.6f
        b.badgeScholar.alpha = 0.6f
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
