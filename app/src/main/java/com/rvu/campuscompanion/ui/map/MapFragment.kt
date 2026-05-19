package com.rvu.campuscompanion.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.model.CampusLocation
import com.rvu.campuscompanion.databinding.FragmentMapBinding
import com.rvu.campuscompanion.utils.Constants
import com.rvu.campuscompanion.viewmodel.MapViewModel

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _b: FragmentMapBinding? = null
    private val b get() = _b!!
    private val vm: MapViewModel by viewModels()
    private var map: GoogleMap? = null

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) enableMyLocation() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMapBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val mapFrag = childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFrag.getMapAsync(this)

        b.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                vm.search(p0?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        b.fabMyLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) enableMyLocation()
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val rvu = LatLng(Constants.RVU_LAT, Constants.RVU_LNG)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rvu, Constants.RVU_ZOOM))
        googleMap.uiSettings.isZoomControlsEnabled = true

        vm.locations.observe(viewLifecycleOwner) { list ->
            googleMap.clear()
            list.forEach { loc -> addMarker(googleMap, loc) }
        }

        googleMap.setOnMarkerClickListener { marker ->
            (marker.tag as? CampusLocation)?.let { showSheet(it) }
            true
        }
    }

    private fun addMarker(map: GoogleMap, loc: CampusLocation) {
        val opt = MarkerOptions()
            .position(LatLng(loc.latitude, loc.longitude))
            .title(loc.name)
            .snippet(loc.timings)
            .icon(BitmapDescriptorFactory.defaultMarker(loc.markerColor))
        map.addMarker(opt)?.tag = loc
    }

    private fun showSheet(location: CampusLocation) {
        LocationSheet(location) { openDirections(location) }
            .show(childFragmentManager, "loc_sheet")
    }

    private fun openDirections(loc: CampusLocation) {
        val uri = Uri.parse("google.navigation:q=${loc.latitude},${loc.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        if (intent.resolveActivity(requireActivity().packageManager) != null) startActivity(intent)
        else startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${loc.latitude},${loc.longitude}")))
    }

    private fun enableMyLocation() {
        try { map?.isMyLocationEnabled = true } catch (_: SecurityException) {}
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null; map = null }
}
