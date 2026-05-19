package com.rvu.campuscompanion.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.databinding.FragmentSplashBinding
import com.rvu.campuscompanion.viewmodel.AuthViewModel

class SplashFragment : Fragment() {
    private var _b: FragmentSplashBinding? = null
    private val b get() = _b!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSplashBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed
            val target = if (vm.isLoggedIn()) R.id.action_splash_to_dashboard
                         else R.id.action_splash_to_login
            findNavController().navigate(target)
        }, 2000)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
