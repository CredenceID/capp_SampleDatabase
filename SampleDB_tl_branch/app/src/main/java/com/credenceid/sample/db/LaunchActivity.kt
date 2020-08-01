package com.credenceid.sample.db

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.credenceid.biometrics.Biometrics
import com.credenceid.biometrics.Biometrics.ResultCode.*
import com.credenceid.biometrics.BiometricsManager

class LaunchActivity : Activity() {
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    private val REQUEST_CODE_PERMISSIONS = 101
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //        camera_preview.setVisibility(View.INVISIBLE);
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            this.initBiometrics()
        }
    }

    private fun initBiometrics() {

        /*  Create new biometrics object. */
        App.BioManager = BiometricsManager(applicationContext)

        /* Initialize object, meaning tell CredenceService to bind to this application. */
        App.BioManager!!.initializeBiometrics { resultCode: Biometrics.ResultCode,
                                                _: String,
                                                _: String ->

            when(resultCode) {
                OK -> {
                    Toast.makeText(this, getString(R.string.bio_init), LENGTH_SHORT).show()

                    App.DevFamily = App.BioManager!!.deviceFamily
                    App.DevType = App.BioManager!!.deviceType

                    /* Launch main activity. */
                    val intent = Intent(this, DatabaseActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    this.finish()
                }
                INTERMEDIATE -> {
                    /* This code is never returned for this API. */
                }
                FAIL ->
                    Toast.makeText(this, getString(R.string.bio_init_fail), LENGTH_LONG).show()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {

        var allgranted = true
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                allgranted = false
            }
        }
        if (allgranted == true) {
            this.initBiometrics()
        } else {
            finish()
        }
    }
}