package com.simpleloan.lscam.UI.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.simpleloan.lscam.Others.SignOutListener
import com.simpleloan.lscam.R
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : AppCompatActivity() , GoogleApiClient.OnConnectionFailedListener , SignOutListener {

    val INITIAL_REQUEST = 1000

    internal val INITIAL_PERMS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    lateinit var context: Context

    private lateinit var mLocationManager: LocationManager

    lateinit var mGoogleApiClient:GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        context=this

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title="LSCam"
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

    }


    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST)
            }
        }


        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, mLocationListener)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnSignOutClick() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(object : ResultCallback<Status> {
            override fun onResult(status: Status) {

            }
        })
    }


    fun canAccessLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, perm)
    }

    private val mLocationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {

        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {
            // Toast.makeText(mContext, "GPS is Enabled.", Toast.LENGTH_SHORT).show();
        }

        override fun onProviderDisabled(provider: String) {
           // Toast.makeText(context, "GPS is Disabled.", Toast.LENGTH_SHORT).show();
            // startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
             showSettingsAlert()
        }
    }


    fun showSettingsAlert() {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("GPS settings")
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            dialog.cancel()
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}
