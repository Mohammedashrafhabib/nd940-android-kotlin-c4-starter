package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    private lateinit var locName: String

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val LocationServices by lazy { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity()) }

    lateinit var latLng:LatLng
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
      binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)

//        TODO: call this function after the user confirms on the selected location
        binding.savebtn.setOnClickListener {

        onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if(latLng!=null){
        _viewModel.longitude.value=latLng.longitude
        _viewModel.latitude.value=latLng.latitude
        _viewModel.reminderSelectedLocationStr.value=locName}
        else{
            Toast.makeText(
                this.context,
                "Please Chose A Location with a name",
                Toast.LENGTH_SHORT
            ).show()
        }
        findNavController().popBackStack()

    }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener {latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            if(snippet !=null){
            this.latLng=latLng
                this.locName=snippet
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            ) } else{
                Toast.makeText(
                    this.context,
                    "Please Chose A Location with a name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            enableGeo()

        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }
    private val REQUEST_LOCATION_PERMISSION = 1
    private fun isPermissionGranted() : Boolean {
        return this.context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION)
        } === PackageManager.PERMISSION_GRANTED
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        } else {
            enableMyLocation()

        }
    }
    private fun isGeoPermissionGranted() : Boolean {
        return this.context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } === PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("MissingPermission")
    private fun enableGeo() {
        if (isGeoPermissionGranted()) {

        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {

            map = p0
        enableMyLocation()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    LocationServices.removeLocationUpdates(this)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))

                }
            }}
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 6000

        LocationServices.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        setMapLongClick(map)
    setPoiClick(map)
        setMapStyle(map)
    }
    private fun setMapStyle(map: GoogleMap) {
        try {

            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
                    R.raw.map_style
                )
            )


        } catch (e: Resources.NotFoundException) {
            Log.e("error", "Can't find style. Error: ", e)
        }
    }
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }
}

