package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.requireContext(), GeofenceBroadcastReceiver::class.java)

        PendingIntent.getBroadcast(this.requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(this.requireContext())

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            if(isPermissionGranted()&&isGeoPermissionGranted())
                checkDeviceLocationSettingsAndStartGeofence()
            else{
                enableMyLocation()
            }
        }
    }

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
        enableGeo()


    } else {
        Toast.makeText(context,"This application require your location to fully work", Toast.LENGTH_LONG).show()

    }
}
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            enableGeo()
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }
    private fun enableGeo() {
        if (isGeoPermissionGranted()) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this.requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this.requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(context,sendEx.message, Toast.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value
                val reminderDataItem=ReminderDataItem(title,description,location, latitude, longitude)
                _viewModel.validateAndSaveReminder(reminderDataItem)
                if(_viewModel.validateEnteredData(reminderDataItem))
                {  val geofence = Geofence.Builder()
                    // Set the request ID, string to identify the geofence.
                    .setRequestId(reminderDataItem.id)
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        latitude!!,
                        longitude!!,
                        1000f
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                    val geofencingRequest = GeofencingRequest.Builder()

                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

                        .addGeofence(geofence)
                        .build()



                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                        addOnSuccessListener {

                        }
                        addOnFailureListener {
                            Toast.makeText(context, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT).show()

                        }
                    }
                }            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
    private  val REQUEST_TURN_DEVICE_LOCATION_ON = 29
}
